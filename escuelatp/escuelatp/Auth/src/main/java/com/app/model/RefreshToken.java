package com.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * A diferencia del JWT de acceso (que no se puede revocar, es la
 * naturaleza de un token stateless), el refresh token SÍ vive en esta
 * base y SÍ lo podemos invalidar a mano — por eso el logout "de verdad"
 * pasa por acá: revocar el refresh token evita que se puedan seguir
 * pidiendo access tokens nuevos, aunque el último access token emitido
 * siga técnicamente vivo hasta que expira solo (por diseño, dura poco).
 */
@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @ManyToOne
    private Usuario usuario;

    private Instant expiraEn;

    private boolean revocado;
}
