package main

import (
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

	org := "btieger"
	caPEM, certPEM, certKeyPEM, err := generateCert([]string{org}, dnsNames, commonName)
	if err != nil {
		errorLogger.Fatalf("Failed to generate ca and certificate key pair: %v", err)
	}

	os.WriteFile("/pems/ca.pem", caPEM.Bytes(), 0644)
	os.WriteFile("/pems/cert.pem", certPEM.Bytes(), 0644)
	os.WriteFile("/pems/certKey.pem", certKeyPEM.Bytes(), 0644)	
}
