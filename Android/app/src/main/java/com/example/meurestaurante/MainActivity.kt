package com.example.meurestaurante

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.meurestaurante.network.RetrofitClient

data class Prato(
    val id: Int,
    val nome: String,
    val descricao: String,
    val preco: Double,
    val categoria: String,
    val imagem: String
)

data class ItemCarrinho(
    val prato: Prato,
    var quantidade: Int
)

val pratosEstaticos = listOf(
    Prato(1, "Pizza Margherita", "Tomate, mozzarella e manjericão", 9.50, "Pizzas", "🍕"),
    Prato(2, "Pizza de Frango", "Frango, queijo e cogumelos", 11.00, "Pizzas", "🍕"),
    Prato(3, "Hambúrguer", "Carne, queijo, alface e tomate", 10.00, "Hambúrgueres", "🍔"),
    Prato(4, "Hambúrguer Especial", "Carne, bacon, queijo e molho especial", 12.50, "Hambúrgueres", "🍔"),
    Prato(5, "Carbonara", "Massa, bacon, queijo e ovo", 9.00, "Massas", "🍝"),
    Prato(6, "Lasanha", "Lasanha de carne com queijo", 10.50, "Massas", "🍝"),
    Prato(7, "Bacalhau", "Bacalhau assado com batata", 13.50, "Pratos", "🐟"),
    Prato(8, "Água", "Água mineral 50cl", 1.50, "Bebidas", "💧"),
    Prato(9, "Coca-Cola", "Coca-Cola 33cl", 2.00, "Bebidas", "🥤"),
    Prato(10, "Tiramisu", "Sobremesa italiana", 4.50, "Sobremesas", "🍰")
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RestauranteApp()
        }
    }
}

@Composable
fun RestauranteApp() {

    var pagina by remember { mutableStateOf("inicio") }
    var pratos by remember { mutableStateOf(pratosEstaticos) }

    LaunchedEffect(Unit) {
        try {
            val pratosDoServidor = RetrofitClient.instance.getPratos()
            if (pratosDoServidor.isNotEmpty()) {
                pratos = pratosDoServidor
            }
        } catch (e: Exception) {
            // Se falhar a ligação (servidor desligado/sem internet), usa a lista de exemplo
            e.printStackTrace()
        }
    }

    val carrinho = remember {
        mutableStateListOf<ItemCarrinho>()
    }

    when (pagina) {

        "inicio" -> {

            PaginaInicial(
                quantidadeCarrinho = carrinho.sumOf { it.quantidade },
                onMenu = {
                    pagina = "menu"
                },
                onCarrinho = {
                    pagina = "carrinho"
                }
            )
        }

        "menu" -> {

            Menu(
                pratos = pratos,
                carrinho = carrinho,

                onAdicionar = { prato ->

                    val item = carrinho.find {
                        it.prato.id == prato.id
                    }

                    if (item != null) {
                        item.quantidade++
                    } else {
                        carrinho.add(
                            ItemCarrinho(prato, 1)
                        )
                    }
                },

                onCarrinho = {
                    pagina = "carrinho"
                },

                onVoltar = {
                    pagina = "inicio"
                }
            )
        }

        "carrinho" -> {

            Carrinho(
                carrinho = carrinho,

                onAumentar = { item ->
                    item.quantidade++
                },

                onDiminuir = { item ->

                    if (item.quantidade > 1) {
                        item.quantidade--
                    } else {
                        carrinho.remove(item)
                    }
                },

                onRemover = { item ->
                    carrinho.remove(item)
                },

                onFinalizar = {
                    pagina = "pedido"
                },

                onVoltar = {
                    pagina = "menu"
                }
            )
        }

        "pedido" -> {

            FazerPedido(
                onConfirmar = {
                    carrinho.clear()
                    pagina = "confirmacao"
                },

                onVoltar = {
                    pagina = "carrinho"
                }
            )
        }

        "confirmacao" -> {

            Confirmacao(
                onInicio = {
                    pagina = "inicio"
                }
            )
        }
    }
}


@Composable
fun PaginaInicial(
    quantidadeCarrinho: Int,
    onMenu: () -> Unit,
    onCarrinho: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "🍴",
            fontSize = 70.sp
        )

        Text(
            text = "Meu Restaurante",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Comida deliciosa à sua espera!",
            fontSize = 17.sp
        )

        Spacer(modifier = Modifier.height(35.dp))

        Button(
            onClick = onMenu,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🍽️  VER MENU")
        }

        Spacer(modifier = Modifier.height(15.dp))

        OutlinedButton(
            onClick = onCarrinho,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🛒  CARRINHO ($quantidadeCarrinho)")
        }
    }
}


@Composable
fun Menu(
    pratos: List<Prato>,
    carrinho: List<ItemCarrinho>,
    onAdicionar: (Prato) -> Unit,
    onCarrinho: () -> Unit,
    onVoltar: () -> Unit
) {

    var categoriaSelecionada by remember {
        mutableStateOf("Todos")
    }

    val categorias = listOf(
        "Todos",
        "Pizzas",
        "Hambúrgueres",
        "Massas",
        "Pratos",
        "Bebidas",
        "Sobremesas"
    )

    val pratosFiltrados =
        if (categoriaSelecionada == "Todos") {
            pratos
        } else {
            pratos.filter {
                it.categoria == categoriaSelecionada
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "🍽️ Menu",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            TextButton(
                onClick = onCarrinho
            ) {
                Text(
                    "🛒 ${carrinho.sumOf { it.quantidade }}"
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {

            item {

                Text(
                    text = "Categorias",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    categorias.take(3).forEach { categoria ->

                        FilterChip(
                            selected = categoriaSelecionada == categoria,

                            onClick = {
                                categoriaSelecionada = categoria
                            },

                            label = {
                                Text(categoria)
                            },

                            modifier = Modifier.padding(end = 5.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    categorias.drop(3).forEach { categoria ->

                        FilterChip(
                            selected = categoriaSelecionada == categoria,

                            onClick = {
                                categoriaSelecionada = categoria
                            },

                            label = {
                                Text(categoria)
                            },

                            modifier = Modifier.padding(end = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))
            }

            items(pratosFiltrados) { prato ->

                PratoCard(
                    prato = prato,
                    onAdicionar = {
                        onAdicionar(prato)
                    }
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }
        }

        Button(
            onClick = onCarrinho,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                "🛒 Ver Carrinho (${carrinho.sumOf { it.quantidade }})"
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        TextButton(
            onClick = onVoltar
        ) {
            Text("← Voltar")
        }
    }
}


@Composable
fun PratoCard(
    prato: Prato,
    onAdicionar: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(15.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = prato.imagem,
                fontSize = 45.sp
            )

            Spacer(
                modifier = Modifier.width(15.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = prato.nome,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = prato.descricao,
                    fontSize = 13.sp
                )

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = String.format(
                        "%.2f€",
                        prato.preco
                    ),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onAdicionar
            ) {
                Text("+")
            }
        }
    }
}


@Composable
fun Carrinho(
    carrinho: List<ItemCarrinho>,
    onAumentar: (ItemCarrinho) -> Unit,
    onDiminuir: (ItemCarrinho) -> Unit,
    onRemover: (ItemCarrinho) -> Unit,
    onFinalizar: () -> Unit,
    onVoltar: () -> Unit
) {

    val total = carrinho.sumOf {
        it.prato.preco * it.quantidade
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "🛒 Carrinho",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        if (carrinho.isEmpty()) {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "🛒",
                    fontSize = 60.sp
                )

                Text(
                    text = "O carrinho está vazio.",
                    fontSize = 20.sp
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                Button(
                    onClick = onVoltar
                ) {
                    Text("Ver Menu")
                }
            }

        } else {

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {

                items(carrinho) { item ->

                    ItemCarrinhoCard(
                        item = item,
                        onAumentar = {
                            onAumentar(item)
                        },
                        onDiminuir = {
                            onDiminuir(item)
                        },
                        onRemover = {
                            onRemover(item)
                        }
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )
                }
            }

            HorizontalDivider()

            Spacer(
                modifier = Modifier.height(15.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "TOTAL",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = String.format(
                        "%.2f€",
                        total
                    ),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.height(15.dp)
            )

            Button(
                onClick = onFinalizar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("FINALIZAR PEDIDO")
            }

            TextButton(
                onClick = onVoltar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("← Continuar a comprar")
            }
        }
    }
}


@Composable
fun ItemCarrinhoCard(
    item: ItemCarrinho,
    onAumentar: () -> Unit,
    onDiminuir: () -> Unit,
    onRemover: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(15.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = item.prato.imagem,
                    fontSize = 35.sp
                )

                Spacer(
                    modifier = Modifier.width(10.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = item.prato.nome,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = String.format(
                            "%.2f€",
                            item.prato.preco
                        )
                    )
                }

                TextButton(
                    onClick = onRemover
                ) {
                    Text("🗑️")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Button(
                    onClick = onDiminuir
                ) {
                    Text("-")
                }

                Text(
                    text = "${item.quantidade}",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(
                        horizontal = 20.dp
                    )
                )

                Button(
                    onClick = onAumentar
                ) {
                    Text("+")
                }
            }
        }
    }
}


@Composable
fun FazerPedido(
    onConfirmar: () -> Unit,
    onVoltar: () -> Unit
) {

    var nome by remember {
        mutableStateOf("")
    }

    var mesa by remember {
        mutableStateOf("")
    }

    var observacoes by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "📝 Finalizar Pedido",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(25.dp)
        )

        OutlinedTextField(
            value = nome,
            onValueChange = {
                nome = it
            },
            label = {
                Text("Nome")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(15.dp)
        )

        OutlinedTextField(
            value = mesa,
            onValueChange = {
                mesa = it
            },
            label = {
                Text("Número da mesa")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(15.dp)
        )

        OutlinedTextField(
            value = observacoes,
            onValueChange = {
                observacoes = it
            },
            label = {
                Text("Observações")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(
            modifier = Modifier.height(25.dp)
        )

        Button(
            onClick = onConfirmar,
            enabled = nome.isNotBlank() && mesa.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("CONFIRMAR PEDIDO")
        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        TextButton(
            onClick = onVoltar
        ) {
            Text("← Voltar")
        }
    }
}


@Composable
fun Confirmacao(
    onInicio: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "✅",
            fontSize = 70.sp
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Text(
            text = "Pedido realizado!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(15.dp)
        )

        Text(
            text = "Obrigado pela sua preferência.",
            fontSize = 18.sp
        )

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Button(
            onClick = onInicio
        ) {
            Text("Voltar ao início")
        }
    }
}