> 原文链接: http://blog.mrcroxx.com/posts/code-reading/boltdb-made-simple/0-introduction/


munmap() errormmap(minsz int) error## 引言
boltdb使用单内存映射文件作为存储（single memory-mapped file on disk）。boltdb在启动时会通过mmap系统调用将数据库文件映射到内存，这样可以仅通过内存访问来对文件进行读写，而将磁盘I/O交给操作系统管理，只有在事务提交或更新元数据时，boltdb才会通过fdatasyc系统调用强制将脏页落盘，以保证事务的ACID语义。

在linux系统中，内存与磁盘间的换入换出是以页为单位的。为了充分利用这一特定，boltdb的数据库文件也是按页组织的，且页大小与操作系统的页大小相等。

由于mmap与unmmap系统调用的开销相对较大，因此boltdb在每次mmap时会预留一部分空间（小于1GB时倍增，超过1GB时每次额外申请1GB），这会产生一些空闲的页；同时，随着对数据库的操作，在更新值注1或删除值时，数据库也可能产生空闲页注2。为了高效地管理这些空闲页，boltdb学习操作系统引入了一个简化的空闲页列表。

boltdb的页与空闲页列表的实现分别在`page.go`与`freelist.go`中，本文主要围绕这两个文件，分析boltdb中页与空闲页列表的设计与实现。同时，本文后半部分介绍了boltdb的读写操作与缓存策略。由于boltdb的读写（特别是写入）与事务关系较为密切，因此本文后半部分的分析中可能涉及到一些与事务相关的代码，不了解boltdb事务实现的读者可以先阅读本系列后续的文章。

> **注1:**
> 为了在保证隔离性的同时支持“读读并发”、“读写并发”（boltdb不支持“写写并发”，即同一时刻只能有一个执行中的可写事务），boltdb在更新页时采用了[Shadow Paging](https://en.wikipedia.org/wiki/Shadow_paging)技术，其通过[copy-on-write](https://en.wikipedia.org/wiki/Copy-on-write)实现。在可写事务更新页时，boltdb首先会复制原页，然后在副本上更新，再将引用修改为新页上。这样，当可写事务更新页时，只读事务还可以读取原来的页；当创建读写事务时，boltdb会释放不再使用的页。这样，便实现了在支持“读读并发”、“读写并发”的同时保证事务的隔离性。
> **注2:**
> boltdb不会将空闲的页归还给系统。其原因有二：
	1.  在不断增大的数据库中，被释放的页之后还会被重用。
	2.  boltdb为了保证读写并发的隔离性，使用copy-on-write来更新页，因此会在任意位置产生空闲页，而不只是在文件末尾产生空闲页（详见[issue#308](https://github.com/boltdb/bolt/issues/308#issuecomment-74811638)）。


## page
### page的总体结构
boltdb中每个页的元数据保存在该页的开头处，我们可以将其看做页的头部 _Page Header_，页的其余部分为页的数据 _Page Body_，不同用途的页的 _Page Body_ 中的数据存储格式不同。
![[Pasted image 20221229163203.png]]
页相关的代码主要在`page.go`中。_Page Header_ 是以`page`结构体表示的，其包含的字段如下:
```go
type pgid uint64

type page struct {
	id       pgid    // 页id。页id从0开始，随地址空间单调递增。
	flags    uint16  // 页标识，用来表示页的类型（用途）。
	count    uint16  // 页中元素个数。
	overflow uint32  // 溢出页个数。当单页无法容纳数据时，可以用与该页相邻的页保存溢出的数据（详见后文中介绍）。
	ptr      uintptr // 页的数据（Page Body）的起始位置。
}
```

boltdb中的页共有三种用途：
- 保存数据库的元数据（_meta page_）
- 保存空闲页列表(_freelist page_)
- 保存数据
因为boltdb中数据是按照B+树组织的，因此保存数据的页又可分为分支节点（_branch page_）和叶子节点（_leaf page_）两种。也就是说，boltdb中页的类型共有4种。

### page的数据结构
本节将分别介绍boltdb中_meta page_、_leaf page_ 与 _branch page_ 的数组结构，_free page_ 的数据结构与其行为关系较为密切，将在本文之后的章节中介绍。

#### meta page
_meta page_ 是boltdb记录数据库元数据的页。_meta page_ 的格式非常简单，其 _Page Body_ 就是一个`meta`结构体。`meta`结构体的字段如下：
```go
type meta struct {
	magic    uint32		// 一个固定值，用来标识该文件为boltdb数据库文件。
	version  uint32		// 用来标识该文件采用的数据库版本号。
	pageSize uint32		// 用来标识改文件采用的页大小。
	flags    uint32		// 保留字段，未使用。
	root     bucket		// boltdb记录根bucket的结构体，其包含了该bucket的根页id与bucket编号（单调递增）。
	freelist pgid		// 空闲页列表的首页id。
	pgid     pgid		// 下一个分配的页id，即当前最大页id+1，用于mmap扩容时为新页编号。
	txid     txid		// 下一个事务的id，全局单调递增。
	checksum uint64		// meta页的校验和。
}

// bucket.go

type bucket struct {
	root     pgid   // page id of the bucket's root-level page
	sequence uint64 // monotonically incrementing, used by NextSequence()
}
```
#### branch page & leaf page
*branch page* 与 *leaf page* 是boltdb中用来保存B+树节点的页。==B+树的分支节点仅用来保存索引（key），而叶子节点既保存索引，又保存值（value）。==
boltdb支持任意长度的key和value，因此无法直接结构化保存key和value的列表。为了解决这一问题，branch page和leaf page的Page Body起始处是一个由定长的索引（branchPageElement或leafPageElement）组成的列表，第$i$个索引记录了第$i$个key或key/value的起始位置与key的长度或key/value各自的长度：
```go
// branchPageElement represents a node on a branch page.
type branchPageElement struct {
	pos   uint32
	ksize uint32
	pgid  pgid
}

// key returns a byte slice of the node key.
func (n *branchPageElement) key() []byte {
	buf := (*[maxAllocSize]byte)(unsafe.Pointer(n))
	return (*[maxAllocSize]byte)(unsafe.Pointer(&buf[n.pos]))[:n.ksize]
}
```
![[Pasted image 20221229175556.png]]
```go
// leafPageElement represents a node on a leaf page.
type leafPageElement struct {
	flags uint32
	pos   uint32
	ksize uint32
	vsize uint32
}

// key returns a byte slice of the node key.
func (n *leafPageElement) key() []byte {
	buf := (*[maxAllocSize]byte)(unsafe.Pointer(n))
	return (*[maxAllocSize]byte)(unsafe.Pointer(&buf[n.pos]))[:n.ksize:n.ksize]
}

// value returns a byte slice of the node value.
func (n *leafPageElement) value() []byte {
	buf := (*[maxAllocSize]byte)(unsafe.Pointer(n))
	return (*[maxAllocSize]byte)(unsafe.Pointer(&buf[n.pos+n.ksize]))[:n.vsize:n.vsize]
}
```
![[Pasted image 20221229180329.png]]
### page溢出结构
虽然B+树会拆分过大的节点，但当key或value过大时，或freelist过大时，不适合将其拆分为多个page。因此，boltdb允许过大的页的数据溢出到之后紧挨着的连续的页中，如下图所示：
![[Pasted image 20221229180413.png]]
如上图所示，一个页和其溢出页共用该页的 _Page Header_，即溢出页只有 _Page Body_ 部分。
这样做的好处是，因为溢出页与首页是连续的且溢出页只有 _Page Body_，那么相当于数据的内存地址是连续的，访问数据时只需要正常计算偏移量即可，不需要特殊处理溢出页。溢出页的数量记录在首页的 _Page Header_ 的`overflow`字段中。
## freelist
boltdb通过`freelist`实现了空闲页列表。boltdb也将`freelist`按照一定格式持久化存储在了`page`中。当数据库初始化或者恢复时，如果能够找到保存在页中的`freelist`，则直接使用该`freelist`，否则扫描数据库，构建新的`freelist`。
### freelist的逻辑结构
`freelist`结构体中有3个字段
```go

// freelist represents a list of all pages that are available for allocation.
// It also tracks pages that have been freed but are still in use by open transactions.
type freelist struct {
	ids     []pgid          // all free and available free page ids.
	pending map[txid][]pgid // mapping of soon-to-be free page ids by tx.
	cache   map[pgid]bool   // fast lookup of all free and pending page ids.
}
```
| 字段                    | 描述                                                                         |
| ----------------------- | ---------------------------------------------------------------------------- |
| ids []pgid              | 记录已释放的页id的有序列表                                                   |
| pending map[txid][]pgid | 事务id到事务待释放的页id                                                     |
| cache map[pgid]bool     | 用来快速查找给定id的页是否被释放的缓存, 现在`ids`和`pending`中的页id均为true |
由于boltdb是通过copy-on-write的方式实现的读写事务并发的隔离性，因此当事务可写事务更新页时，其会复制已有的页，并将旧页加入到`pending`中该事务id下的待释放页的列表中。因为此时可能还有读事务在读取旧页，所以不能立刻释放该页，而是要等到所有事务都不再依赖该页时，才能将`pending`中的页加入到`ids`中。对于boltdb中事务的实现笔者会在本系列后面的文章中介绍，这里不再赘述，这里读者只需要了解`pending`的作用即可。

这样做的好处还有，当事务回滚时，可以重用`pending`中还未释放的页（由于该事务还未提交，因此其之前释放的所有页都可被重用）。而且，重用页时对freelist的操作十分简单，只需要将`pending`中该事务id对应的列表清空即可。
### freelist的存储结构
当数据库将freelist写入page时，会将`ids`与`pending`中的页id合并在一起写入`ids`。因为如果数据库crash了，那么所有事务都会终止，`pending`中的页都可以安全释放。

因此，保存freelist的page只需要写入有序的空闲页id列表即可，其结构如下：
![[Pasted image 20221229201333.png]]
freelist页溢出的处理方式与其它page稍有不同。由于`page`结构体的`count`字段为`uint16`类型，其最大值为65535（`0xFFFF`），假设页大小为4KB，那么`count`字段能表示的最大的freelist只能记录256MB的页。也就是说，即使允许freelist的page溢出，但是由于受`count`字段的限制，其仍无法表示足够大的空间。因此，boltdb在写入freelist的page时，会判断空闲页列表的长度。当空闲页列表长度小于`0xFF`时，采用与其它的类型相同的方式处理；而当空闲页列表长度大于等于`0xFF`时，则用本应写入第一条pgid的位置（`ptr`指向的位置）记录空闲页列表的真实长度，而将真正的空闲页列表往后顺延一个条目的位置写入，同时将`count`置为`0xFF`。其示意图如下：
![[Pasted image 20221229201505.png]]
从page读取freelist与将freelist写入page的相应方法如下：
```go
// read initializes the freelist from a freelist page.
func (f *freelist) read(p *page) {
	// If the page.count is at the max uint16 value (64k) then it's considered
	// an overflow and the size of the freelist is stored as the first element.
	idx, count := 0, int(p.count)
	if count == 0xFFFF {
		idx = 1
		count = int(((*[maxAllocSize]pgid)(unsafe.Pointer(&p.ptr)))[0])
	}

	// Copy the list of page ids from the freelist.
	if count == 0 {
		f.ids = nil
	} else {
		ids := ((*[maxAllocSize]pgid)(unsafe.Pointer(&p.ptr)))[idx:count]
		f.ids = make([]pgid, len(ids))
		copy(f.ids, ids)

		// Make sure they're sorted.
		sort.Sort(pgids(f.ids))
	}

	// Rebuild the page cache.
	f.reindex()
}

// write writes the page ids onto a freelist page. All free and pending ids are
// saved to disk since in the event of a program crash, all pending ids will
// become free.
func (f *freelist) write(p *page) error {
	// Combine the old free pgids and pgids waiting on an open transaction.

	// Update the header flag.
	p.flags |= freelistPageFlag

	// The page.count can only hold up to 64k elements so if we overflow that
	// number then we handle it by putting the size in the first element.
	lenids := f.count()
	if lenids == 0 {
		p.count = uint16(lenids)
	} else if lenids < 0xFFFF {
		p.count = uint16(lenids)
		f.copyall(((*[maxAllocSize]pgid)(unsafe.Pointer(&p.ptr)))[:])
	} else {
		p.count = 0xFFFF
		((*[maxAllocSize]pgid)(unsafe.Pointer(&p.ptr)))[0] = pgid(lenids)
		f.copyall(((*[maxAllocSize]pgid)(unsafe.Pointer(&p.ptr)))[1:])
	}

	return nil
}
```
### freelist的方法

`release`调用的时机为新读写事务启动时。在启动新的读写事务时，boltdb会根据事务id释放所有已完成的事务在`pending`中的页：
```go
func (db *DB) beginRWTx() (*Tx, error) {
	
	// ... ...

	// Free any pages associated with closed read-only transactions.
	var minid txid = 0xFFFFFFFFFFFFFFFF
	for _, t := range db.txs {
		if t.meta.txid < minid {
			minid = t.meta.txid
		}
	}
	if minid > 0 {
		db.freelist.release(minid - 1)
	}

	return t, nil
}
```

## boltdb的读写与缓存策略
boltdb的缓存策略主要有两种，一种是使用mmap的读缓存策略，一种是使用page buffer的写缓存策略。

### 读操作与缓存策略
boltdb在读取数据库文件时，为了避免频繁进行设备I/O，使用了mmap技术作为缓存。当boltdb打开数据库时，其会将数据库文件通过mmap系统调用映射到内存。这样可以避免使用read系统调用读取I/O设备，而是直接以内存访问的方式读取数据。在通过mmap将数据库文件映射到内存后，boltdb会根据数据库文件构建内存数据结构，如meta、freelist、B+Tree结构。

根据使用方式的不同，meta、freelist、B+Tree使用mmap中数据的方式各不相同。

boltdb将其meta直接指向了mmap内存空间的meta page，但仅用来读取，不会直接修改meta page。当创建新事务时，boltdb会复制当前的meta page到一处内存中，作为该事务开始时的meta快照。

freelist和B+Tree都是根据mmap内存空间的page在内存别处构建的数据结构，但二者的构建策略不同。freelist是在打开数据库时完整地读取mmap内存空间中的freelist page构建的；而B+Tree则是在使用中按需构建的，即在读取B+Tree的node时，如果node已经在缓存中构建过，则读取已经构建好的缓存，如果node还没在缓存中构建过，则读取mmap内存空间中的数据，在内存别处构建node的缓存。

boltdb的读操作与缓存策略可通过下图表示：
![[Pasted image 20221229203335.png]]
#### boltdb数据库文件结构
boltdb的数据库文件由两个meta页、一个freelist页、和若干个用来保存数据与索引的B+树的branchNode页和leafNode页组成（页可能包含若干个overflow页）。当数据库初始化时，其会将0、1号页初始化为meta页、将2号页初始化为freelist页、将3号页初始化为空的leafNodePage。

由于只有B+树的页是通过copy-on-write方式写入的，所以boltdb设置了两个meta页以进行本地容错。在更新元数据时，boltdb会交替写入两个meta页。这样，如果meta页写入中途数据库挂掉，数据库仍可以使用另一份完整的meta页。
#### mmap
mmap是boltdb的主要缓存策略。与mmap相关的方法主要有`mmap`、`munmap`、`mmapSize`(位于`db.go`中)：
| 方法                            | 描述                                                                                                                        |
| ------------------------------- | --------------------------------------------------------------------------------------------------------------------------- |
| mmap(minsz int) error           | 以内存映射文件的方式打开数据库文件并初始化meta引用。参数`minsz`是最小的mmap大小，其实际mmap大小是通过`mmapSize`方法获取的。 |
| munmap() error                  | 取消文件的内存映射。                                                                                                        |
| mmapSize(size int) (int, error) | 计算mmap大小，参数`size`是最小大小。                                                                                        |
|                                 |                                                                                                                             |
boltdb中mmap会调用Linux的系统调用，其`prot`参数为`PROT_READ`，`flags`为`MAP_SHARED`与数据库配置中`MmapFlags`按位或的结果。
boltdb的mmap大小增长策略如下：最小的mmap大小为32KB，在1GB之前mmap大小每次倍增，在1GB之后每次增长1GB。
> 注:
> boltdb在进行映射时，mmap大小可能超过数据库文件大小。在访问超出文件大小的mmap部分时会引起`SIGBUS`异常。为了避免访问到超出文件的部分，同时尽可能减少对底层文件大小的的不必要的增长，boltdb选择的是在事务提交时按需增长底层文件大小的策略。其具体实现详见本系列后续文章[深入浅出boltdb —— 0x04 事务](http://blog.mrcroxx.com/posts/code-reading/boltdb-made-simple/4-transaction/)。

### 写操作与缓存策略
为了保证事务的ACID性质，数据库系统需要确保事务提交时文件的修改要安全地写入到磁盘或其它I/O设备。由于随机的设备I/O非常耗时，boltdb不会在数据更新时立刻修改底层文件。而是先将修改后的page写入到一块page buffer内存中作为缓存，等事务提交时，再将事务修改的所有page buffer顺序地写入I/O设备。同时，为了保证数据被安全地写入到了I/O设备，boltdb的刷盘采用pwrite+fdatasyc实现。

boltdb的写操作与缓存策略可通过下图表示：
![[Pasted image 20221229203925.png]]
#### page buffer（memory->memory）
无论是修改meta、freelist，还是修改或写入新B+Tree的node时，boltdb都会先将数据按照page结构写入mmap内存空间外的page buffer中，等到事务提交时再将page buffer中数据写入到底层数据库文件相应的page处。
分配page buffer的逻辑在`db.go`的`allocate`方法中：
```go

// allocate returns a contiguous block of memory starting at a given page.
func (db *DB) allocate(count int) (*page, error) {
	// Allocate a temporary buffer for the page.
	var buf []byte
	if count == 1 {
		buf = db.pagePool.Get().([]byte)
	} else {
		buf = make([]byte, count*db.pageSize)
	}
	p := (*page)(unsafe.Pointer(&buf[0]))
	p.overflow = uint32(count - 1)

	// Use pages from the freelist if they are available.
	if p.id = db.freelist.allocate(count); p.id != 0 {
		return p, nil
	}

	// Resize mmap() if we're at the end.
	p.id = db.rwtx.meta.pgid
	var minsz = int((p.id+pgid(count))+1) * db.pageSize
	if minsz >= db.datasz {
		if err := db.mmap(minsz); err != nil {
			return nil, fmt.Errorf("mmap allocate error: %s", err)
		}
	}

	// Move the page id high water mark.
	db.rwtx.meta.pgid += pgid(count)

	return p, nil
}
```
通过这段代码可以看出，`allocate`方法会先创建一段能够容纳给定数量page的连续内存空间，这段空间就是page buffer，然后将这段空间作为page返回给调用者。这样，调用者可以向读写正常page一样读写这段page buffer。

为了避免分配较小的page buffer造成过多的内存碎片，boltdb通过`sync.Pool`类型的字段`pagePool`分配、复用长度为1的page buffer。`pagePool`的定义与初始化代码如下：
```go

	// in struct db
	pagePool sync.Pool

	// in `db.Open`
	// Initialize page pool.
	db.pagePool = sync.Pool{
		New: func() interface{} {
			return make([]byte, db.pageSize)
		},
	}
```
在分配page buffer时，需要分配的page buffer长度为1，boltdb会通过`pagePool`分配；在事务提交时，boltdb将page buffer中的数据写入到文件后，会将长度为1的page buffer放回`pagePool`。而如果所需的page buffer长度大于1，则boltdb会通过`make`分配page buffer的内存空间。

#### pwrite + fdatasync（memory->disk）
为了保证事务的ACID性质，当事务提交时，boltdb需要保证数据被完整地写入到了磁盘中。在介绍boltdb的数据同步策略前，笔者首先简单介绍Linux系统提供的文件数据同步方式。

在Linux中，为了性能考虑，`write/pwrite`等系统调用不会等待设备I/O完成后再返回。`write/pwrite`等系统调用只会更新page cache，而脏页的同步时间由操作系统控制。`sync`系统调用会在page cache中的脏页提交到设备I/O队列后返回，但是不会等待设备I/O完成。如果此时I/O设备故障，则数据还可能丢失。而`fsync`与`fdatasync`则会等待设备I/O完成后返回，以提供最高的同步保证。`fsync`与`fdatasync`的区别在于，`fdatasync`只会更新文件数据和必要的元数据（如文件大小等），而`fsync`会更新文件数据和所有相关的元数据（包括文件修改时间等），由于文件元数据与数据的保存位置可能不同，因此在磁盘上`fsync`往往比`fdatasync`多一次旋转时延。

对于内存映射文件，Linux提供了`msync`系统调用。该系统调用可以更精确地控制同步的内存范围。

虽然boltdb使用了内存映射文件，但是当事务提交时，其还是通过`pwrite + fdatasync`的方式同步刷盘。在Linux的文档中并没有详细说明混用普通文件的同步方式与内存映射文件的同步方式的影响。但是通过实践和mmap的`MAP_SHARED`模式的描述可知，使用SHARED的mmap，当其它进程通过`fdatasync`等系统调用修改底层文件后，修改能通过mmap的内存访问到。

```go
// write writes any dirty pages to disk.
func (tx *Tx) write() error {
	// Sort pages by id.
	pages := make(pages, 0, len(tx.pages))
	for _, p := range tx.pages {
		pages = append(pages, p)
	}
	// Clear out page cache early.
	tx.pages = make(map[pgid]*page)
	sort.Sort(pages)

	// Write pages to disk in order.
	for _, p := range pages {
		size := (int(p.overflow) + 1) * tx.db.pageSize
		offset := int64(p.id) * int64(tx.db.pageSize)

		// Write out page in "max allocation" sized chunks.
		ptr := (*[maxAllocSize]byte)(unsafe.Pointer(p))
		for {
			// Limit our write to our max allocation size.
			sz := size
			if sz > maxAllocSize-1 {
				sz = maxAllocSize - 1
			}

			// Write chunk to disk.
			buf := ptr[:sz]
			if _, err := tx.db.ops.writeAt(buf, offset); err != nil {
				return err
			}

			// Update statistics.
			tx.stats.Write++

			// Exit inner for loop if we've written all the chunks.
			size -= sz
			if size == 0 {
				break
			}

			// Otherwise move offset forward and move pointer to next chunk.
			offset += int64(sz)
			ptr = (*[maxAllocSize]byte)(unsafe.Pointer(&ptr[sz]))
		}
	}

	// Ignore file sync if flag is set on DB.
	if !tx.db.NoSync || IgnoreNoSync {
		if err := fdatasync(tx.db); err != nil {
			return err
		}
	}

	// Put small pages back to page pool.
	for _, p := range pages {
		// Ignore page sizes over 1 page.
		// These are allocated using make() instead of the page pool.
		if int(p.overflow) != 0 {
			continue
		}

		buf := (*[maxAllocSize]byte)(unsafe.Pointer(p))[:tx.db.pageSize]

		// See https://go.googlesource.com/go/+/f03c9202c43e0abb130669852082117ca50aa9b1
		for i := range buf {
			buf[i] = 0
		}
		tx.db.pagePool.Put(buf)
	}

	return nil
}
```
从上面这段来自`tx.go`的代码中，我们可以看到：在事务提交写入磁盘时，boltdb会先将事务使用的page buffer按照页id排序，然后将page buffer中的内容通过文件的`WriteAt`方法（go语言通过`pwrite`系统调用实现）写入到底层数据库文件中。在完成所有`pwrite`的调用后，boltdb通过`fdatasync`系统调用等待数据安全地同步到磁盘中。最后，boltdb将待释放的长度为1的page buffer放回到`pagePool`中，以复用这些单页，减少内存碎片的影响。

## 总结
总的来说，boltdb的读写操作与缓存策略可以概括如下：
-   读数据库文件时，通过mmap技术将其映射到内存中，避免频繁的设备I/O；不同的数结构在不同时刻读取mmap内存空间的数据完成构建。
-   写数据库文件时，通过page buffer先写入内存缓存，然后在事务提交时将page buffer中的数据通过pwrite + fdatasync系统调用写入底层文件并确保写入的安全性。

而连接mmap与pwrite + fdatasync的桥梁，是操作系统。操作系统保证了通过`MAP_SHARED`映射的内存空间在底层文件修改时会更新。