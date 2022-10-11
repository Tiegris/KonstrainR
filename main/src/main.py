from Crypto.PublicKey import RSA

def read_pem(fname: str) -> str:
    with open(fname, 'r') as f:
        data = f.read()
    return RSA.import_key(data)