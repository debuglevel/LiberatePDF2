### Building stage
#FROM node:14.7.0-alpine AS builder
#WORKDIR /src/
#
## cache dependencies
#COPY package*.json /src/
#RUN npm install
#
## build source
#COPY . /src/
#RUN npm run build
#
### Final image
#FROM nginx:1.15
#RUN rm -rf /usr/share/nginx/html/*
#COPY --from=builder /src/docker/nginx.conf /etc/nginx/conf.d/default.conf
##COPY --from=builder /src/dist/out/ /usr/share/nginx/html
#COPY --from=builder /src/src/ /usr/share/nginx/html
#
## create settings.json file based on environment variables on startup
##CMD ["/bin/sh",  "-c",  "envsubst < /usr/share/nginx/html/assets/settings.template.json > /usr/share/nginx/html/assets/settings.json && exec nginx -g 'daemon off;'"]


## the above does not work too well, and I don't know how deployment was supposed to work in that ancient Angular version.
## therefore darn it, I'm just using the development server until I ported it to a current version or another framework
FROM node:14.7.0-alpine AS builder
WORKDIR /src/

# cache dependencies
COPY package*.json /src/
RUN npm install

# build source
COPY . /src/
EXPOSE 3000
CMD ["npm", "run", "serve"]