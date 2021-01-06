# LiberatePDF 2

LiberatePDF2 is a frontend to `pdftk input.pdf input_pw $password output output.pdf allow AllFeatures`, e.g. it removes the PDF encryption and removes all restrcitions (e.g. printing disabled, commenting disabled, et cetera).

![Angular client of LiberatePDF2](images/angular.png)

## Architecture

The frontend is made with Angular. It connects to a REST backend written in Kotlin and the Micornaut framework.

## Build

Simpelst method would be starting the `docker-compose.yml` with `docker-compose up --build` which spins up a Nginx container at `http://localhost:8082` which serves the angular client, and the REST backend container at `http://localhost:8081`.

For building the angular client and the REST service without Docker, see the `README.md` in their subdirectories.

## Configuration

Main settings which can be configured are described in `docker-compose.yml`.
