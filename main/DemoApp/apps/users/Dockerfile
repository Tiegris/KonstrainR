FROM python:alpine
RUN pip install Flask
WORKDIR /app
COPY ./main.py .
ENTRYPOINT ["python3", "main.py"]
