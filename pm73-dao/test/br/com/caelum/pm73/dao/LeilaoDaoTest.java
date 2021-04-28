package br.com.caelum.pm73.dao;

import br.com.caelum.pm73.dao.builder.LeilaoBuilder;
import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.Usuario;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LeilaoDaoTest {
    private Session session;
    private UsuarioDao usuarioDao;
    private LeilaoDao leilaoDao;

    @Before
    public void setUp(){
        session = new CriadorDeSessao().getSession();
        usuarioDao = new UsuarioDao(session);
        leilaoDao = new LeilaoDao(session);

        session.beginTransaction();
    }

    @After
    public void teadDown(){
        session.getTransaction().rollback();
        session.close();
    }

    @Test
    public void deveContarLeiloesNaoEncontrados(){
        Usuario mauricio = new Usuario("Mauricio", "mauricio@email.com");

        Leilao ativo = new LeilaoBuilder()
                .comDono(mauricio)
                .constroi();

        Leilao encerrado = new LeilaoBuilder()
                .comDono(mauricio)
                .encerrado()
                .constroi();

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(ativo);
        leilaoDao.salvar(encerrado);

        long total = leilaoDao.total();

        assertEquals(1L, total);
    }

    @Test
    public void deveRetornarZero(){
        Usuario mauricio = new Usuario("Mauricio", "mauricio@email.com");

        Leilao encerrado1 = new LeilaoBuilder()
                .comDono(mauricio)
                .encerrado()
                .constroi();

        Leilao encerrado2 = new LeilaoBuilder()
                .comDono(mauricio)
                .encerrado().constroi();

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(encerrado1);
        leilaoDao.salvar(encerrado2);

        long total = leilaoDao.total();

        assertEquals(0L, total);
    }

    @Test
    public void deveRetornarLeiloesNaoUsados(){
        Usuario mauricio = new Usuario("Mauricio", "mauricio@email.com");

        Leilao produtoNovo =
                new LeilaoBuilder()
                        .comDono(mauricio)
                        .comNome("XBox")
                        .constroi();
        Leilao produtoUsado =
                new LeilaoBuilder().comNome("XBox")
                        .comDono(mauricio)
                        .usado()
                        .constroi();

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(produtoNovo);
        leilaoDao.salvar(produtoUsado);

        List<Leilao> novos = leilaoDao.novos();

        assertEquals(1, novos.size());
        assertEquals("XBox", novos.get(0).getNome());
    }

    @Test
    public void deveRetornarLeiloesAntigos() {
        Usuario mauricio = new Usuario("Mauricio", "mauricio@email.com");

        Leilao recente = new LeilaoBuilder()
                .comNome("XBox")
                .comDono(mauricio)
                .constroi();
        Leilao antigo = new LeilaoBuilder()
                .comDono(mauricio)
                .comNome("Geladeira")
                .diasAtras(10)
                .constroi();

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(recente);
        leilaoDao.salvar(antigo);

        List<Leilao> antigos = leilaoDao.antigos();

        assertEquals(1, antigos.size());
        assertEquals("Geladeira", antigos.get(0).getNome());
    }

    @Test
    public void deveTrazerSomenteLeiloesAntigosHaMaisDe7Dias()  {
        Usuario mauricio = new Usuario("Mauricio", "mauricio@email.com");

        Leilao noLimite = new LeilaoBuilder()
                .diasAtras(7)
                .comDono(mauricio)
                .constroi();

        Leilao recente = new LeilaoBuilder()
                .comNome("XBox")
                .comDono(mauricio)
                .constroi();

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(noLimite);
        leilaoDao.salvar(recente);

        List<Leilao> antigos = leilaoDao.antigos();

        assertEquals(1, antigos.size());
        assertEquals("XBox", antigos.get(0).getNome());
    }


    @Test
    public void deveTrazerleiloesNaoEncerradosNoPeriodo(){
        Calendar comecoDoIntervalo = Calendar.getInstance();
        comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -10);

        Calendar fimDoIntervalo = Calendar.getInstance();

        Usuario mauricio = new Usuario("Mauricio", "mauricio@email.com");

        Leilao leilao1 = new LeilaoBuilder()
                .diasAtras(2)
                .comDono(mauricio)
                .comNome("XBox")
                .constroi();

        Leilao leilao2 = new LeilaoBuilder()
                .diasAtras(20)
                .comDono(mauricio)
                .comNome("XBox")
                .constroi();

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);

        List<Leilao> leilaos = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);
        assertEquals(1, leilaos.size());
        assertEquals("XBox", leilaos.get(0).getNome());
    }


    @Test
    public void naoDeveTrazerLeiloesEncerradosNoPeriodo(){
        Calendar comecoDoIntervalo = Calendar.getInstance();
        comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -10);

        Calendar fimDoIntervalo = Calendar.getInstance();

        Usuario mauricio = new Usuario("Mauricio", "mauricio@email.com");

        Leilao leilao1 = new LeilaoBuilder()
                .comDono(mauricio)
                .diasAtras(2)
                .comNome("XBox")
                .encerrado()
                .constroi();

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(leilao1);

        List<Leilao> leilaos = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);
        assertEquals(0, leilaos.size());
    }

    @Test
    public void deveRetornarLeiloesDisputados() {
        Usuario mauricio = new Usuario("Mauricio", "mauricio@aniche.com.br");
        Usuario marcelo = new Usuario("Marcelo", "marcelo@aniche.com.br");

        Leilao leilao1 = new LeilaoBuilder()
                .comDono(marcelo)
                .comValor(3000.0)
                .comLance(Calendar.getInstance(), mauricio, 3000.0)
                .comLance(Calendar.getInstance(), marcelo, 3100.0)
                .constroi();

        Leilao leilao2 = new LeilaoBuilder()
                .comDono(mauricio)
                .comValor(3200.0)
                .comLance(Calendar.getInstance(), mauricio, 3000.0)
                .comLance(Calendar.getInstance(), marcelo, 3100.0)
                .comLance(Calendar.getInstance(), mauricio, 3200.0)
                .comLance(Calendar.getInstance(), marcelo, 3300.0)
                .comLance(Calendar.getInstance(), mauricio, 3400.0)
                .comLance(Calendar.getInstance(), marcelo, 3500.0)
                .constroi();

        usuarioDao.salvar(marcelo);
        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);

        List<Leilao> leiloes = leilaoDao.disputadosEntre(2500, 3500);

        assertEquals(1, leiloes.size());
        assertEquals(3200.0, leiloes.get(0).getValorInicial(), 0.00001);
    }


    @Test
    public void listaSomenteOsLeiloesDoUsuario() throws Exception {
        Usuario dono = new Usuario("Mauricio", "m@a.com");
        Usuario comprador = new Usuario("Victor", "v@v.com");
        Usuario comprador2 = new Usuario("Guilherme", "g@g.com");

        Leilao leilao = new LeilaoBuilder()
                .comDono(dono)
                .comValor(50.0)
                .comLance(Calendar.getInstance(), comprador, 100.0)
                .comLance(Calendar.getInstance(), comprador2, 200.0)
                .constroi();

        Leilao leilao2 = new LeilaoBuilder()
                .comDono(dono)
                .comValor(250.0)
                .comLance(Calendar.getInstance(), comprador2, 100.0)
                .constroi();

        usuarioDao.salvar(dono);
        usuarioDao.salvar(comprador);
        usuarioDao.salvar(comprador2);
        leilaoDao.salvar(leilao);
        leilaoDao.salvar(leilao2);

        List<Leilao> leiloes = leilaoDao.listaLeiloesDoUsuario(comprador);
        assertEquals(1, leiloes.size());
        assertEquals(leilao, leiloes.get(0));
    }

    @Test
    public void listaDeLeiloesDeUmUsuarioNaoTemRepeticao() throws Exception {
        Usuario dono = new Usuario("Mauricio", "m@a.com");
        Usuario comprador = new Usuario("Victor", "v@v.com");
        Leilao leilao = new LeilaoBuilder()
                .comDono(dono)
                .comLance(Calendar.getInstance(), comprador, 100.0)
                .comLance(Calendar.getInstance(), comprador, 200.0)
                .constroi();
        usuarioDao.salvar(dono);
        usuarioDao.salvar(comprador);
        leilaoDao.salvar(leilao);

        List<Leilao> leiloes = leilaoDao.listaLeiloesDoUsuario(comprador);
        assertEquals(1, leiloes.size());
        assertEquals(leilao, leiloes.get(0));
    }

    @Test
    public void devolveAMediaDoValorInicialDosLeiloesQueOUsuarioParticipou(){
        Usuario dono = new Usuario("Mauricio", "m@a.com");
        Usuario comprador = new Usuario("Victor", "v@v.com");
        Leilao leilao = new LeilaoBuilder()
                .comDono(dono)
                .comValor(50.0)
                .comLance(Calendar.getInstance(), comprador, 100.0)
                .comLance(Calendar.getInstance(), comprador, 200.0)
                .constroi();
        Leilao leilao2 = new LeilaoBuilder()
                .comDono(dono)
                .comValor(250.0)
                .comLance(Calendar.getInstance(), comprador, 100.0)
                .constroi();
        usuarioDao.salvar(dono);
        usuarioDao.salvar(comprador);
        leilaoDao.salvar(leilao);
        leilaoDao.salvar(leilao2);

        assertEquals(150.0, leilaoDao.getValorInicialMedioDoUsuario(comprador), 0.001);
    }

    @Test
    public void deveDeletarLeilao(){
        Usuario mauricio = new Usuario("Mauricio", "mauricio@email.com");

        Leilao leilao = new LeilaoBuilder()
                .comDono(mauricio)
                .constroi();

        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(leilao);
        leilaoDao.deleta(leilao);

        session.flush();
        session.clear();

        Leilao leilaoDeletado = leilaoDao.porId(1);

        assertNull(leilaoDao.porId(leilao.getId()));

    }
}