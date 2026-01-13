# Use official Node.js image as the base image
FROM node:latest as build

# Set working directory in the container
WORKDIR /app

# Copy package.json and package-lock.json to container
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy all files from the current directory to the container
COPY . .

# Build React app
RUN npm run build

# Use Nginx image as the base image for serving the React app
FROM nginx:latest

# Copy build files from previous stage to Nginx server's html directory
COPY --from=build /app/build /usr/share/nginx/html

# Expose port 80 to the outside world
EXPOSE 80

# Command to run Nginx
CMD ["nginx", "-g", "daemon off;"]
