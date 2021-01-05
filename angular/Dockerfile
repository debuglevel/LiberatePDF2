## Building stage
FROM node:14.7.0-alpine AS builder
WORKDIR /src/

# cache dependencies
COPY package*.json /src/
RUN npm install

# build source
COPY . /src/
RUN npm run build --prod --output-path=./dist

RUN ls -R /src/dist

## Final image
FROM nginx:1.15
RUN rm -rf /usr/share/nginx/html/*
COPY --from=builder /src/docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /src/docker/settings.template.json /usr/share/nginx/html/assets/
COPY --from=builder /src/dist/liberatepdf2-angular/ /usr/share/nginx/html

# create settings.json file based on environment variables on startup
CMD ["/bin/sh",  "-c",  "envsubst < /usr/share/nginx/html/assets/settings.template.json > /usr/share/nginx/html/assets/settings.json && exec nginx -g 'daemon off;'"]
