package iuh.fit.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * Base entity cung cấp soft-delete cho tất cả entity kế thừa.
 * - deletedAt = NULL  → bản ghi đang hoạt động
 * - deletedAt != NULL → bản ghi đã bị xóa mềm
 *
 * @SQLRestriction tự động thêm điều kiện "deleted_at IS NULL" vào mọi query JPA.
 */
@Getter
@Setter
@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
public abstract class BaseEntity {

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.deletedAt = null;
    }
}
