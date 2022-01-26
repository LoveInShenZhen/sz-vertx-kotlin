package main

import "go.uber.org/zap"

func main() {
	println("go 是个弱鸡 !!!")
	logger, _ := zap.NewDevelopment()
	logger = logger.With(zap.String("service_id", "data-srver-01"))
	defer logger.Sync()

	logger.Info("服务启动成功", )
	logger.Warn("go 是个弱鸡 !!!")
}
