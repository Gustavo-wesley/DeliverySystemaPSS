package com.ufes.delivery.repository;

import com.ufes.delivery.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface IUsuarioRepository {

    Usuario salvar(Usuario usuario);

    void atualizar(Usuario usuario);

    void excluir(Long id);

    /**
     * Atualiza a situacao/perfil de varios usuarios em transacao unica.
     */
    void atualizarTodos(List<Usuario> usuarios);

    Optional<Usuario> buscarPorUsername(String username);

    Optional<Usuario> buscarPorId(Long id);

    /**
     * Busca por nome civil ou nome de usuario, insensivel a maiusculas.
     */
    List<Usuario> buscarPorNome(String trecho);

    List<Usuario> buscarTodos();

    long contar();
}
