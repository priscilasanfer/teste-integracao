### *01. Escrevendo o primeiro teste de integração*

*P:* Qual a diferença entre um teste de integração e um teste de unidade?  
*R:* Um teste de unidade isola a classe de suas dependências, e a testa independente delas.  
Testes de unidade fazem sentido quando nossas classes contém regras de negócio, mas dependem de infra estrutura.   
Nesses casos, fica fácil isolar a infra estrutura.  
Já testes de integração testam a classe de maneira integrada ao serviço que usam.   
Um teste de DAO, por exemplo, que bate em um banco de dados de verdade, é considerado um teste de integração.   
Testes como esses são especialmente úteis para testar classes cuja responsabilidade é se comunicar com outros serviços.

*P:* Quais são os problemas de se usar mocks (e simular a conexão com o banco de dados) para testar DAOs?  
*R:* Ao usar mocks, estamos "enganando" nosso teste.  
Um bom teste de DAO é aquele que garante que sua consulta SQL realmente funciona quando enviada para o banco de dados; 
e a melhor maneira de garantir isso é enviando-a para o banco!
Repare que, na explicação, quando usamos mock objects, nosso teste passou, mesmo estando com bug!  
Testes como esses não servem de nada, apenas atrapalham.
Sempre que tiver classes cuja responsabilidade é falar com outro sistema, teste-a realmente integrando com esse outro sistema.  

*P:* Por que precisamos fechar a Session ao final do teste?  
*R:* Precisamos fechar a sessão para que o banco de dados libere essa porta para um próximo teste.  
Não fechar a sessão pode implicar em problemas futuros, como testes que falham ou travam. Lembre-se sempre de fechar a conexão.

### *02. Organizando o teste de integração*

*P:* O que geralmente fazemos no fim de cada teste de integração?    
*R:* Podemos fazer diversas coisas ao final de cada teste.  
Mas uma coisa que geralmente não é opcional é limpar o banco de dados para que o próximo teste consiga executar sem problemas.  
Fazemos isso dando rollback no banco de dados, ou mesmo executando uma sequência de TRUNCATE TABLEs.   
Você pode escolher qual maneira agrada mais!  

### *03. Praticando com consultas mais complicadas*

*P:* Quais são os maiores desafios na escrita de testes de integração?  
*R:* Geralmente o grande desafio é justamente montar o cenário e validar o resultado esperado no sistema externo.   
No caso de banco de dados, precisamos fazer INSERTs, DELETEs, e SELECTs para validar.   
Além disso, ainda precisamos manter o estado do sistema consistente, ou seja, precisamos limpar o banco de dados constantemente para que um teste não atrapalhe o outro.  
Veja que usamos o JUnit da mesma forma.   
A diferença é que justamente precisamos nos comunicar com o outro sistema.

### *04. Testando alteração e deleção* 

*P:* Para que serve o "flush" no meio do teste?  
*R:* Precisamos forçar o Hibernate a enviar comandos para o banco de dados, e garantir que as próximas consultas levarão em consideração as anteriores.   
Para isso, o flush() torna-se obrigatório em alguns testes!  
Geralmente em testes que fazemos SELECTs logo após uma deleção ou alteração em batch, o uso do flush é obrigatório.   

*P:* O que você acha de testar métodos de alteração como os que você acabou de testar?  
*R:* Tudo é questão de feedback. Eles podem fazer sentido quando o processo de alteração é complicado.   
Por exemplo, atualizar um usuário é bem simples e feito pelo próprio Hibernate. Não há muito como dar errado.  
Agora, atualizar um leilão pode ser mais trabalhoso, afinal precisamos atualizar lances juntos.   
Nesses casos, testar uma alteração pode ser bem importante.  
