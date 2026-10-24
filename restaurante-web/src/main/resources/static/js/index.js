const params = new URLSearchParams(window.location.search);
const unidadeNome = params.get("unidade") || "";
document.getElementById("titulo-unidade").textContent = unidadeNome || "Selecione uma unidade";

async function carregarProdutos() {
    if (!unidadeNome) return;
    const resp = await fetch(`/api/produtos?unidade=${encodeURIComponent(unidadeNome)}`);
    const produtos = await resp.json();
    const lista = document.getElementById("lista-produtos");
    lista.innerHTML = "";
    produtos.forEach(p => {
        // Busca a imagem principal e o preço atual via endpoints ou inclua na resposta
        const card = document.createElement("div");
        card.className = "card";
        card.onclick = () => abrirDetalhes(p.produtoId);
        card.innerHTML = `
                    <img src="${p.imagemPrincipalUrl || 'https://via.placeholder.com/200'}" alt="${p.produtoNome}">
                    <h3>${p.produtoNome}</h3>
                    <p class="preco">R$ ${p.precoAtual || '0,00'}</p>
                `;
        lista.appendChild(card);
    });
}

async function abrirDetalhes(produtoId) {
    const resp = await fetch(`/api/produtos/${produtoId}`);
    const p = await resp.json();
    document.getElementById("popup-nome").textContent = p.produtoNome;
    document.getElementById("popup-descricao").textContent = p.produtoDescricao;
    document.getElementById("popup-preco").textContent = "R$ " + (p.precoAtual || "0,00");
    const divImgs = document.getElementById("popup-imagens");
    divImgs.innerHTML = "";
    (p.imagens || []).forEach(img => {
        const el = document.createElement("img");
        el.src = img.imagemProdutoURL;
        divImgs.appendChild(el);
    });
    document.getElementById("popup").style.display = "flex";
}

carregarProdutos();