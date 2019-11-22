/**
 * MarkPage - Markdown Page 
 *
 * An easy way to to write markdown document base on `showdownjs`, support table, code hightlight , mermaid charts and navigate.
 *
 * Just import one 'markpage.js' file in your html file, 
 *
 * And go ahead to writing your markdown document in a <pre id="markpage"> or <textarea id="markpage"> html tag
 *
 * It can be translated into HTML automatically, and generate a simple navigate.
 *
 * The markpage.js will load other js/css file automatically. So,
 *
 * You only need to care about the specific content of all you have to write
 *
 * The final html file like as follow:
 * ----------------------------------------------------------
 *
 * <textarea id="markpage">
 * ###markdown document
 * writing you document in here
 * </textarea>
 *
 * <!-- only need to import markpage.js file  -->
 * <script src="markpage.js" type="text/javascript"></script
 *
 * ----------------------------------------------------------
 *
 * NOTE: Not support for IE8 or lower version. To make life better, upgrade your browser.
 *
 * @author qinwei
 * @email  qinwei081@gmail.com
 *
 **/

/**
 * Thansk for those open source libary bellow, and you can change the url if you want, such as from cdnjs 
 **/
var ResConfig = {
    marx_css: 'http://cdn.bootcss.com/marx/1.3.0/marx.min.css',
    mermaid_css: 'http://cdn.bootcss.com/mermaid/0.5.8/mermaid.min.css',

    zepto_js: 'http://cdn.bootcss.com/zepto/1.1.6/zepto.min.js',
    showdown_js: 'http://cdn.bootcss.com/showdown/1.3.0/showdown.min.js',
    highlight_js: 'http://cdn.bootcss.com/highlight.js/8.9.1/highlight.min.js',
    mermaid_js: 'http://cdn.bootcss.com/mermaid/0.5.8/mermaid.min.js'
};
(function() {
    function markpage() {
        var self = this;
        var resourceNumExpect = 4; // expect load js resource number
        var loadedResource = 0; // current loaded
        var onLoadCallback = function() {};

        function checkResourceLoad() {
            loadedResource++;
            if (loadedResource == resourceNumExpect) {
                resourceOnload();
            }
        }

        this.mkdom = function(name, attr) {
            attr = attr || {};
            var dom = document.createElement(name);
            for (var i in attr) {
                dom.setAttribute(i, attr[i])
            }
            return dom;
        };

        this.addChildNode = function(tagName, childNode) {
            var head = document.getElementsByTagName(tagName)[0];
            head.appendChild(childNode);
        };

        this.loadcss = function(link) {
            var linkNode = self.mkdom('link', {
                rel: 'stylesheet',
                type: "text/css",
                href: link
            });
            self.addChildNode('head', linkNode);
        }
        this.loadjs = function(src, onloadCallback) {
            var jsnode = self.mkdom('script', {
                type: 'text/javascript',
                src: src
            });
            self.addChildNode('body', jsnode);
            jsnode.onload = onloadCallback;
        }

        this.loadDocCss = function() {
            var css = [
               'h1 {padding:0 0 10px 0;}'
               ,'.docmenu { position:relative;float:left; height:100%; width:20%; }'
               ,'.docmenu .menu { position:fixed; width:20%; overflow-y: scroll; overflow-x: hidden; height: 100%; background-color: #fff;}'
               ,'h2, h3, h4{ padding: 0 0 5px 0; margin-top:1.5em; }'
               ,'h3, h4{ margin-bottom:0.5em; }'
               ,'h2 {border-bottom: 1px solid #eee; margin-bottom:16px;}'
               ,'ul li {margin: 16px 5px;}'
               ,'p {line-height: 2;}'
               ,'code { padding: 2px 4px; font-size: 90%; color: #c7254e; background-color: #f9f2f4; border-radius: 4px; }'
               ,'main { float:left; max-width: 80%;}'
               ,'@media print {.docmenu { display:none; }  }'
               ,'@media screen and (max-width:60rem) {'
               , 'main { max-width:100%;width:100%;}'
               , '.docmenu {position: relative; width: 100%;height:auto;}'
               , '.docmenu .menu { position:relative; width:100%;}'
               , '}'
            ];
            // can't not use $ before zepto/jquery load
            var style = self.mkdom('style', {
                type: 'text/css'
            });
            style.appendChild(document.createTextNode(css.join('')));
            self.addChildNode('head', style);
        }

       /**
        * This function will invoke when all resource loaded
        */
        function resourceOnload() {
            initShowdownExt();
            hljs.initHighlightingOnLoad();
            mermaidAPI.initialize({
                startOnLoad: false
            });
            mermaid.parseError = function(err, hash) {
                console.error(err);
            };

            onLoadCallback.call(self);
        }

        this.loadResource = function(cb) {
            self.loadcss(ResConfig.marx_css);
            self.loadcss(ResConfig.mermaid_css);
            self.loadDocCss();
            // expect 3 js resource to load
            self.loadjs(ResConfig.zepto_js, checkResourceLoad);
            self.loadjs(ResConfig.showdown_js, checkResourceLoad);
            self.loadjs(ResConfig.highlight_js, checkResourceLoad);
            self.loadjs(ResConfig.mermaid_js, checkResourceLoad);
            if (typeof cb == 'function') {
                onLoadCallback = cb;
            }
        }

        self.onload = function(cb) {
            onLoadCallback = cb;
        }

        this.render = function(selector) {
            var $doc = $(selector).hide();
            var theme = $doc.attr('data-highlight-theme') || 'railscasts';
            self.loadcss('http://cdn.bootcss.com/highlight.js/8.9.1/styles/' + theme + '.min.css');
            $('body').append('<div class="docmenu"></div><main><section class="docbody"></section></main><footer>Render By MarkPage</footer>');
            $('#markpage').hide();
            var text = $doc.html();
            text = text.replace(/&gt;/ig, ">");
            text = text.replace(/&lt;/ig, "<");
            text = text.replace(/&amp;/ig, "&");
        /**
         * The Document of showdownjs At: https://github.com/showdownjs/showdown
         */
            var converter = new showdown.Converter({
                tables: true,
                //table support
                tasklists: true,
                //tasklists support
                strikethrough: true,
                // Enable support for strikethrough syntax. ~~HH~~
                literalMidWordUnderscores: true,
                // ignore `list_by_name` transfer to <em> or <strong> 
                omitExtraWLInCodeBlocks: true,
                parseImgDimensions: true,
                ghCodeBlocks: true,
                extensions: ['markpage']
            }),
            html = converter.makeHtml(text);

            //render mermaid charts
            $('.docbody').append(html).find('.mermaid').each(function(i, el) {
                var $this = $(el);
                var cb = function(svgGraph) {
                    var $svg = $('<div />').html(svgGraph).find('svg');
                    $this.html(svgGraph);

                    //resize height to match box
                    var height = parseInt($svg.attr('viewBox').split(/\s+/)[3] || 400); //data like: -50 -10 750 347
                    $this.css('height', (height + 50) + 'px');
                };
                var data = $this.text().replace(/^\s+/, "");
                if (mermaidAPI.parse(data)) {
                    var graphId = 'graph-' + i;
                    mermaidAPI.render(graphId, data, cb, el);
                } else {
                    $this.append('<p style="color:red">Mermaid syntax has error, you can open the console pannel(F12) to view error logger!</p>');
                }
            });
        }
    }
    /**
     * init showdown ext after load showdown.js
     */
    function initShowdownExt() {
        var markdownMarkpageExt = function(converter) {
            return [{
                type: 'output',
                filter: function(text) {
                    var $ext = $('<div>' + text + '</div>');
                    $ext.find('pre').addClass('prettyprint');
                    var $root = $('<ul class="menu H1" />');
                    var $docmenu = $('.docmenu');
                    $docmenu.append($root);

                    $ext.find('h1,h2,h3,h4').each(function(i, el) {
                        var $el = $(el);
                        var tagName = $el[0].tagName;
                        var tagNum = tagName.replace('H', '');
                        var linkHtml = '<a href="#' + $el.attr('id') + '" >' + $el.html() + '</a>';
                        var parentTag = [];
                        for (var i = 1; i <= tagNum; i++) {
                            parentTag.push('.H' + i);
                        }
                        var $parentUL = $docmenu.find(parentTag.join(',')).last();
                        var $li = $('<li />').html(linkHtml);
                        $li.append('<ul class="H' + (++tagNum) + '" />');
                        $parentUL.append($li);
                    });
                    return $ext.html();
                }
            }];
        };

        // Client-side export
        if (typeof window !== 'undefined' && window.showdown && window.showdown.extensions) {
            window.showdown.extensions.markpage = markdownMarkpageExt;
        }
    };

    new markpage().loadResource(function() {
        this.render('#markpage');
    });

})();

