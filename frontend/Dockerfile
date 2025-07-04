# -----------------------------
# Stage 1: Build the React app
# -----------------------------
FROM node:alpine3.22 AS builder

WORKDIR /app

COPY package.json package-lock.json* ./

RUN npm install --frozen-lockfile || npm install

COPY . .

RUN npm run build

# -----------------------------
# Stage 2: Serve the React app
# -----------------------------
FROM node:alpine3.22

WORKDIR /app

RUN npm install -g serve

COPY --from=builder /app/build ./build

EXPOSE 3000

CMD ["serve", "-s", "build", "-l", "3000"]
