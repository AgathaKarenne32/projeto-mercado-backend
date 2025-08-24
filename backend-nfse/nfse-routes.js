const express = require("express");
const routes = express.Router();
const { createNFSe, getAllNFSes, getNFSeById, updateNFSe, deleteNFSe} = require("../controllers/nfse-controller");

router.post("/", createNFSe); //Cadastra Manualmente a nota 
router.get("/", getAllNFSes); // Lista todas as notas
router.get("/:id", getNFSeById); // Lista a nota pela Id
router.put("/:id", updateNFSe); // Atualiza a nota pelo Id 
router.delete("/:id", deleteNFSe); // Deleta a nota pela Id 

module.exports = router;
