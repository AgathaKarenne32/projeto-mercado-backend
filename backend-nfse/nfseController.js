const poll = require("../db")


// Cadastrar manualmente a NFSe

const createNFSe = async (req, res) =>{
    try {
        const{numero_nf, data_emissao, codigo_verificacao, valor, prestador, tomador } = req.body;
        
        const result = await poll.query(
            "INSERT INTO nfse (numero_nf, data_emissao, codigo_verificacao, valor, prestador, tomador) VALUES ($1, $2, $3, $4, $5, $6)",
            [numero_nf, data_emissao, codigo_verificacao, valor, prestador, tomador]
        );

        res.status(201).json({message: "NFSe criada com sucesso!", nfse: result.rows[0] });
    } cath (error) {
    console.log(error);
    res.status(500).json({erro: "Erro ao cadastrar NFSe"})
}

};

// listar todas as NFSes registradas pelo Usuario 

const getAllNFSes = async(req, res) =>{
    try { 
        const result = await.poll.query("SELECT * FROM nfse ORDER BY id DESC");
        res.json(result.rows);
    } cath (error) {
        res.status(500).json({erro: "Erro ao buscar NFSes"});

    }

    };

    // Listar todas as NFSes
}
    ) 