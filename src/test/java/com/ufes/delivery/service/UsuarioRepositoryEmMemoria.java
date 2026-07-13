package com.ufes.delivery.service;

import com.ufes.delivery.model.Usuario;
import com.ufes.delivery.repository.IUsuarioRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Fake em memoria para testes dos services de usuario.
 */
class UsuarioRepositoryEmMemoria implements IUsuarioRepository {

    private final Map<Long, Usuario> dados = new HashMap<>();
    private long proximoId = 1;

    @Override
    public Usuario salvar(Usuario usuario) {
        usuario.setId(proximoId++);
        dados.put(usuario.getId(), usuario);
        return usuario;
    }

    @Override
    public void atualizar(Usuario usuario) {
        dados.put(usuario.getId(), usuario);
    }

    @Override
    public void excluir(Long id) {
        dados.remove(id);
    }

    @Override
    public void atualizarTodos(List<Usuario> usuarios) {
        for (Usuario usuario : usuarios) {
            atualizar(usuario);
        }
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return dados.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public List<Usuario> buscarPorNome(String trecho) {
        String filtro = trecho.toLowerCase(Locale.ROOT);
        return dados.values().stream()
                .filter(u -> u.getNome().toLowerCase(Locale.ROOT).contains(filtro)
                        || u.getUsername().toLowerCase(Locale.ROOT).contains(filtro))
                .toList();
    }

    @Override
    public List<Usuario> buscarTodos() {
        return new ArrayList<>(dados.values());
    }

    @Override
    public long contar() {
        return dados.size();
    }
}
