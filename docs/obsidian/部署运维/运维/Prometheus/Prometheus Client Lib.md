# Prometheus Client
![[Pasted image 20220319122055.png]]
- __Desc__: 该结构是图中最关键的结构体，需要详细说明。
	- __fqName__: 由Namespace、Subsystem和metric的Name组成。相同fqName的监控指标在整个系统中需要保证唯一(dimhash, label dimensions hash)，包括：type、labels(顺序无关)、help。
	- __labels__: 包含该metric的label列表
- __Metric__: Desc相同，但label值不同的指标定义为Metric，即LabelPair列表+Desc确定一个Metric，每个Metric都有其type（counter、gauge、summary、histogram）和vaule
- __MetricVec__: 相同fqName的Metric会存放在同一个MetricVec
- __Collector__: 任何可以被收集监控指标的接口（MetricVec、Metric均实现了该接口）；通常在使用时类似Package，自定义的Collector将多个相关的MetricVec打包，以供收集，例如：goCollector、processCollector。
- __Registry__: 实现了Gatherer(数据收集)和Registerer（指标注册）接口，并提供了以上功能。

# 在生产环境使用的之前需要考虑以下问题：
- 如何分配和包装好fqName，fqName至关重要，将会影响整套监控系统的维护难度。
- 如何定义框架指标和业务指标接口
- 是否为指标添加固定的label，如：服务名、环境、IDC等
- Client不会自动剔除不再使用的Metric(除非服务重启)，是否需要提供Metric [Sink](https://github.com/armon/go-metrics/blob/master/prometheus/prometheus.go)的功能