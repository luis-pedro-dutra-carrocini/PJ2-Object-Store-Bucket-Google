document.getElementById("form-produto").onsubmit = async (e) => {
    e.preventDefault();
    const body = {
        produtoNome: document.getElementById("nome").value,
        produtoDescricao: document.getElementById("descricao").value
    };
    const resp = await fetch("/api/adm/produtos", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body)
    });
    const p = await resp.json();
    alert("Produto criado: " + p.produtoId);
};

document.getElementById("form-imagem").onsubmit = async (e) => {
    e.preventDefault();
    const produtoId = document.getElementById("produto-id").value;
    const file = document.getElementById("arquivo").files[0];
    const principal = document.getElementById("principal").checked ? "SIM" : "NAO";

    const formData = new FormData();
    formData.append("file", file);
    formData.append("principal", principal);

    await fetch(`/api/adm/produtos/${produtoId}/imagens`, {
        method: "POST",
        body: formData
    });
    alert("Imagem enviada");
};