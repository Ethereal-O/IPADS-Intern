GO=go
GRPC_PROTO_DIR=./grpc/proto

all: proto build

proto:
	protoc --go_out=$(GRPC_PROTO_DIR) \
		--go-grpc_out=$(GRPC_PROTO_DIR) \
		$(GRPC_PROTO_DIR)/station.proto

build:
	$(GO) build

run:
	$(GO) run main.go

clean:
	rm -f edgesystem