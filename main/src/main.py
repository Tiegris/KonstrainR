from Crypto.PublicKey import RSA

def read_pem(fname: str) -> str:
    with open(fname, 'r') as f:
        data = f.read()
    return RSA.import_key(data)

def main():
    pem_ca = read_pem('/pems/ca.pem')
    pem_cert = read_pem('/pems/cert.pem')
    pem_certKey = read_pem('/pems/certKey.pem')


if __name__ == "__main__":
    main()