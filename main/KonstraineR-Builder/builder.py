import os
import requests

class Compilation():
    def __init__(self, base_url: str, dsl_id: int):
        self._base_url = base_url.strip('/')
        self._dsl_id = dsl_id
        
    def download(self) -> str:
        dsl_file = requests.get(f'{self._base_url}/dsl/{self._dsl_id}/file')
        return dsl_file.text    
    
    def arrange(self, dsl_text: str):
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
        requests.post(f'{self._base_url}/jars/{self._dsl_id}', data)
        

def main():
    base_url = os.environ.get("KSR_CORE_BASE_URL")
    dsl_id = os.environ.get("KSR_DSL_ID")
    
    c = Compilation(base_url, dsl_id)
    dsl_text = c.download()
    c.arrange(dsl_text)
    c.compile()
    jar = c.read_jar()
    c.upload(jar)

if __name__ == "__main__":
    main()