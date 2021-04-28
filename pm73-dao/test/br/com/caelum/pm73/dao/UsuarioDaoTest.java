package br.com.caelum.pm73.dao;

import br.com.caelum.pm73.dominio.Usuario;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class UsuarioDaoTest {

    private Session session;
    private UsuarioDao usuarioDao;

    @Before
    public void setUp(){
        session = new CriadorDeSessao().getSession();
        usuarioDao = new UsuarioDao(session);
        session.beginTransaction();
    }

    @After
    public void teadDown(){
        session.getTransaction().rollback();
        session.close();
    }

    @Test
    public void deveEncontrarPeloNomeEEmailMockado(){
        Usuario usuarioNovo = new Usuario("Joao da Silva", "joao@email.com");
        usuarioDao.salvar(usuarioNovo);

        Usuario usuario = usuarioDao.porNomeEEmail("Joao da Silva", "joao@email.com");

        assertEquals("Joao da Silva", usuario.getNome());
        assertEquals("joao@email.com", usuario.getEmail());
    }

    @Test
    public void deveRetornarNulo(){
        Usuario usuarioDoBanco = usuarioDao.porNomeEEmail("Joao da Silva", "joao@email.com");
        assertNull(usuarioDoBanco);
    }


    @Test
    public void deveDeletarUsuario(){
        Usuario usuario= new Usuario("Joao da Silva", "joao@email.com");
        usuarioDao.salvar(usuario);
        usuarioDao.deletar(usuario);

        session.flush();
        session.clear();

        Usuario deletado = usuarioDao.porNomeEEmail("Joao da Silva", "joao@email.com");

        assertNull(deletado);
    }

    @Test
    public void deveAlterarUsuario(){
        Usuario usuario= new Usuario("Joao da Silva", "joao@email.com");

        usuarioDao.salvar(usuario);


        usuario.setNome("Priscila Ferreira");
        usuario.setEmail("teste@novoemail.com");

        usuarioDao.atualizar(usuario);

        session.flush();


        Usuario usuarioNovo = usuarioDao.porNomeEEmail("Priscila Ferreira", "teste@novoemail.com");
        assertNotNull(usuarioNovo);


        Usuario usuarioInexistente = usuarioDao.porNomeEEmail("Joao da Silva", "joao@email.com");
        assertNull(usuarioInexistente);

    }


}