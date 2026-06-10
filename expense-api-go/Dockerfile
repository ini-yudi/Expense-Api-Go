FROM golang:1.21-alpine AS builder
WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 go build -o expense-api .

FROM alpine:3.19
WORKDIR /app
COPY --from=builder /app/expense-api .
EXPOSE 8080
CMD ["./expense-api"]
