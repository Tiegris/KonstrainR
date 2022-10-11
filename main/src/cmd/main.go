package main

import (
	"bytes"
	"log"
	"os"
)

var (
	errorLogger   *log.Logger
	webhookNamespace, webhookServiceName string
)

func init() {
	errorLogger = log.New(os.Stderr, "ERROR: ", log.Ldate|log.Ltime|log.Lshortfile)
	webhookNamespace = os.Getenv("POD_NAMESPACE")
	webhookServiceName = os.Getenv("SERVICE_NAME")
}

func main() {
	dnsNames := []string{
		webhookServiceName,
		webhookServiceName + "." + webhookNamespace,
		webhookServiceName + "." + webhookNamespace + ".svc",
	}
	commonName := webhookServiceName + "." + webhookNamespace + ".svc"

	org := "tiegris"
	caPEM, certPEM, certKeyPEM, err := generateCert([]string{org}, dnsNames, commonName)
	if err != nil {
		errorLogger.Fatalf("Failed to generate ca and certificate key pair: %v", err)
	}

	savePem("/pems/ca.pem", caPEM)
	savePem("/pems/cert.pem", certPEM)
	savePem("/pems/certKey.pem", certKeyPEM)
}


func savePem(fname string, data *bytes.Buffer) {
	err := os.WriteFile(fname, data.Bytes(), 0644)
	if err != nil {
		errorLogger.Fatalf("Failed to write file %s %v", fname, err)
	}
}