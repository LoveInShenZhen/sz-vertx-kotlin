# 准备一个python环境
```shell
conda create -n grpc_sample python=3.11

conda activate grpc_sample
```

# 按照依赖
```shell
pip install grpcio

pip install grpcio-tools

# 可选, 如果用的了 grpc 的反射
pip install grpcio-reflection
```

# Download the example
```shell
git clone -b v1.64.0 --depth 1 --shallow-submodules https://github.com/grpc/grpc

```