const express = require("express")
const cors = require("cors")

const nfseRoutes = require ("./routes/nfseRoutes")


const app = express();

app.use(cors());
app.use(express.json());
app.use("/nfse", nfseRoutes);

app.listen(3001, () => {
    console.log("Servidor rodando na porta 3001");
});

app.use((req, res) => {
    res.status(404).json({erro: "Rota não encontrada"});
});