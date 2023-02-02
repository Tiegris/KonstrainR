import os
import requests

class Compilation():
    def __init__(self, base_url: str, dsl_id: int, secret: str):
        self._base_url = base_url.strip('/')
        self._dsl_id = dsl_id
        self._secret = secret

    def download(self) -> str:
        dsl_file = requests.get(f'{self._base_url}/dsl/{self._dsl_id}/file')
        return dsl_file.text

    def arrange(self, dsl_text: str):
        # remove package
        if dsl_text.startswith("package"):
            dsl_text = dsl_text.split("\n",maxsplit=1)
        with open("/app/framework/lib/src/main/kotlin/DslInstance.kt", "w") as f:
            f.write(dsl_text)

    def compile(self):
        os.chdir('/app/framework')
        os.system('./gradlew jar')

    def read_jar(self) -> bytes:
        with open("/app/framework/lib/build/libs/lib.jar", "rb") as f:
            file_bytes = f.read()
        return file_bytes

    def upload(self, data: bytes):
        response = requests.post(f'{self._base_url}/{self._dsl_id}/jar', data, headers={ "Authorization" : self._secret }) # TODO url finalize
        if response.status_code == 200:
            return
        raise Exception(f"Status code of upload was: {response.status_code}, message: {response.text}")


def main():
    base_url = os.environ.get("KSR_CORE_BASE_URL")
    base_url = f'http://{base_url}/api/v1/dsls'
    dsl_id = os.environ.get("KSR_DSL_ID")
    secret = os.environ.get("KSR_SECRET")
    try:
        c = Compilation(base_url, dsl_id, secret)
        dsl_text = c.download()
        c.arrange(dsl_text)
        c.compile()
        jar = c.read_jar()
        c.upload(jar)
    except Exception as e:
        # Report failed build
        requests.post(f'{base_url}/{dsl_id}/jar', str(e), headers={ "Authorization" : secret }) # TODO url finalize

if __name__ == "__main__":
    main()