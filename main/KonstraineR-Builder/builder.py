import os
import sys
import requests
import logging
import subprocess

logger = logging.getLogger("Compilation")
logger.setLevel(logging.INFO)
_handler = logging.StreamHandler(sys.stdout)
_handler.setFormatter(logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s'))
logger.handlers.clear()
logger.addHandler(_handler)

class Compilation():
    def __init__(self, base_url: str, dsl_id: int, secret: str):
        self._base_url = base_url.strip('/')
        self._dsl_id = dsl_id
        self._secret = secret

    def download(self) -> str:
        logger.info("Download started")
        dsl_file = requests.get(f'{self._base_url}/{self._dsl_id}/file')
        logger.info(f"Download finished, status code: {dsl_file.status_code}")
        return dsl_file.text

    def arrange(self, dsl_text: str):
        logger.info("Build arrangement started")

        if dsl_text.startswith("package"):
            dsl_text = dsl_text.split("\n",maxsplit=1)[1]
            logger.info("Package removed")
        
        dsl_text = "package me.btieger\n\n" + dsl_text
        logger.info("Package added")
        
        dsl_text.replace('\r','')
        logger.info("CR characters removed")
        
        with open("/app/framework/lib/src/main/kotlin/me/btieger/DslInstance.kt", "w") as f:
            f.write(dsl_text)
        logger.info("File saved")

    def compile(self):
        os.chdir('/app/framework')
        logger.info("Compilation started")
        result = subprocess.run(['./gradlew', 'jar'], stdout=subprocess.PIPE)
        if result.returncode != 0:
            logger.error("Compilation failed")
            raise Exception(f"Compilation failed, gradle output: {result.stdout}")
        else:
            logger.info("Compilation done")

    def read_jar(self) -> bytes:
        with open("/app/framework/lib/build/libs/lib.jar", "rb") as f:
            file_bytes = f.read()
        logger.info(f"Loaded JAR file to memory. Bytes loaded: {len(file_bytes)}")
        return file_bytes

    def upload(self, data: bytes):
        response = requests.post(f'{self._base_url}/{self._dsl_id}/jar', data, headers={ "Authorization" : self._secret, "Content-Type": "application/java-archive" })
        if response.status_code == 200:
            logger.info(f"Upload was successful")
        else:
            logger.error(f"Upload failed. Status code: {response.status_code}, message: {response.text}")
            raise Exception(f"Status code of upload was: {response.status_code}, message: {response.text}")


def main():
    base_url = os.environ.get("KSR_CORE_BASE_URL")
    base_url = f'http://{base_url}:8080/api/v1/dsls'
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
        try:
            requests.post(f'{base_url}/{dsl_id}/jar', str(e), headers={ "Authorization" : secret, "Content-Type": "text/plain" })
        except:
            logger.fatal(f"There was an error during build and the message could not be uploaded to KontraineR-Core. Message: {e}")
            sys.exit(1)
            
if __name__ == "__main__":
    main()