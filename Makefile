
.PHONY: db-up
db-up: ## Start all services containers in production mode
	docker-compose build
	docker-compose up -d db-generic
	docker-compose up -d db-specific