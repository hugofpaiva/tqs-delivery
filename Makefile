
.PHONY: verifiy
verifiy: ## Start all services containers in production mode
	 docker-compose up -d db-generic
	 docker-compose up -d db-specific