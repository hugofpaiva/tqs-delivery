package ua.tqs.humberpecas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.tqs.humberpecas.model.Produto;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {
}
