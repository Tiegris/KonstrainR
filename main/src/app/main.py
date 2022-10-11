import util
from kubernetes import client, config

def main():
    pems = util.read_pems()
    config.load_incluster_config()
    pass


if __name__ == "__main__":
    main()