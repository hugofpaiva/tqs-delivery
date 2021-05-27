
.PHONY: db-up
db-up: ## Start all postgreSQL containers in production mode
	docker-compose up -d db-generic
	docker-compose up -d db-specific