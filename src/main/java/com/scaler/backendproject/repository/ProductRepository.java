package com.scaler.backendproject.repository;

import com.scaler.backendproject.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySkuIgnoreCase(String sku);

    boolean existsBySkuIgnoreCase(String sku);

    @EntityGraph(attributePaths = "category")
    @Query("""
            select p from Product p
            where p.deleted = false
              and p.active = true
              and (:categorySlug is null or p.category.slug = :categorySlug)
              and (:query is null
                    or lower(p.title) like lower(concat('%', :query, '%'))
                    or lower(p.description) like lower(concat('%', :query, '%'))
                    or lower(p.sku) like lower(concat('%', :query, '%')))
            """)
    Page<Product> searchActive(@Param("query") String query,
                               @Param("categorySlug") String categorySlug,
                               Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("select p from Product p where p.id = :id and p.deleted = false")
    Optional<Product> findActiveById(@Param("id") String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id in :ids and p.deleted = false and p.active = true")
    List<Product> findAllActiveByIdForUpdate(@Param("ids") Collection<String> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id in :ids")
    List<Product> findAllByIdForUpdate(@Param("ids") Collection<String> ids);
}
