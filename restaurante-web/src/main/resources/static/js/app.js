// Utilitários compartilhados entre as telas

// Lê um parâmetro da query string da URL
function getParam(nome) {
    return new URLSearchParams(window.location.search).get(nome);
}

// Formata número para moeda brasileira
function formatarMoeda(valor) {
    const n = Number(valor || 0);
    return n.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

// Wrapper simples para fetch com tratamento de erro
async function apiGet(url) {
    const resp = await fetch(url);
    if (!resp.ok) throw new Error('Erro ao buscar: ' + resp.status);
    return resp.json();
}

async function apiPostJson(url, body) {
    const resp = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    if (!resp.ok) throw new Error('Erro ao salvar: ' + resp.status);
    return resp.json();
}