FROM golang:1.17 as builder
WORKDIR /build

COPY go.mod /build/go.mod
COPY cmd/ /build/cmd/

RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -a -o konstrainer /build/cmd

COPY ./entrypoint.sh /build/entrypoint.sh
COPY ./prestop.sh /build/prestop.sh
RUN chmod +x /build/entrypoint.sh /build/prestop.sh /build/konstrainer

FROM python:3.9-bullseye
WORKDIR /

RUN mkdir pems app

COPY --from=builder /build/konstrainer /pem-init.exe
COPY --from=builder /build/entrypoint.sh /entrypoint.sh
COPY --from=builder /build/prestop.sh /prestop.sh

ENTRYPOINT ["/entrypoint.sh"]
