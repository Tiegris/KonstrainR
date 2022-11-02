from Crypto.PublicKey import RSA
from Crypto.PublicKey.RSA import RsaKey

def read_pem(fname: str) -> RsaKey:
    with open(fname, 'r') as f:
        data = f.read()
    return RSA.import_key(data)

def read_pems() -> tuple[RsaKey, RsaKey, RsaKey]:
    pem_cert = read_pem('/pems/cert.pem')
    pem_certKey = read_pem('/pems/certKey.pem')
    return pem_cert, pem_certKey