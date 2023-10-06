FROM python:alpine
RUN pip install Flask pymongo
WORKDIR /app
COPY ./main.py .
ENTRYPOINT ["python3", "main.py"]
