const pool = require("../db");

// Cadastrar manualmente a NFSe
const createNFSe = async (req, res) => {
    try {
        const { numero_nf, data_emissao, codigo_verificacao, valor, prestador, tomador } = req.body;

        const result = await pool.query(
            "INSERT INTO nfse (numero_nf, data_emissao, codigo_verificacao, valor, prestador, tomador) VALUES ($1, $2, $3, $4, $5, $6) RETURNING *",
            [numero_nf, data_emissao, codigo_verificacao, valor, prestador, tomador]
        );

        res.status(201).json({ message: "NFSe criada com sucesso!", nfse: result.rows[0] });
    } catch (error) {
        console.error(error);
        res.status(500).json({ erro: "Erro ao cadastrar NFSe" });
    }
};

// Listar todas as NFSes
const getAllNFSes = async (req, res) => {
    try {
        const result = await pool.query("SELECT * FROM nfse ORDER BY id DESC");
        res.json(result.rows);
    } catch (error) {
        res.status(500).json({ erro: "Erro ao buscar NFSes" });
    }
};

// Buscar NFSe pelo ID
const getNFSeByID = async (req, res) => {
    try {
        const { id } = req.params;
        const result = await pool.query("SELECT * FROM nfse WHERE id = $1", [id]);

        if (result.rows.length === 0) {
            return res.status(404).json({ erro: "NFSe não encontrada" });
        }

        res.json(result.rows[0]);
    } catch (error) {
        res.status(500).json({ erro: "Erro ao buscar NFSe" });
    }
};

// Atualizar NFSe
const updateNFSe = async (req, res) => {
    try {
        const { id } = req.params;
        const { numero_nf, data_emissao, codigo_verificacao, valor, prestador, tomador } = req.body;

        const result = await pool.query(
            `UPDATE nfse
             SET numero_nf = $1, data_emissao = $2, codigo_verificacao = $3, valor = $4, prestador = $5, tomador = $6, updated_at = NOW()
             WHERE id = $7 RETURNING *`,
            [numero_nf, data_emissao, codigo_verificacao, valor, prestador, tomador, id]
        );

        if (result.rows.length === 0) {
            return res.status(404).json({ erro: "NFSe não encontrada" });
        }

        res.json({ message: "NFSe atualizada com sucesso!", nfse: result.rows[0] });
    } catch (error) {
        console.error(error);
        res.status(500).json({ erro: "Erro ao atualizar NFSe" });
    }
};

// Deletar NFSe
const deleteNFSe = async (req, res) => {
    try {
        const { id } = req.params;

        const result = await pool.query("DELETE FROM nfse WHERE id = $1 RETURNING *", [id]);

        if (result.rows.length === 0) {
            return res.status(404).json({ erro: "NFSe não encontrada" });
        }

        res.json({ message: "NFSe deletada com sucesso!", nfse: result.rows[0] });
    } catch (error) {
        res.status(500).json({ erro: "Erro ao deletar NFSe" });
    }
};

module.exports = {
    createNFSe,
    getAllNFSes,
    getNFSeByID,
    updateNFSe,
    deleteNFSe
};
