const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const fs = require('fs');
const https = require('https');
require('dotenv').config();

const app = express();
app.use(cors());
app.use(express.json());

// MongoDB connection
mongoose.connect(process.env.MONGO_URI)
  .then(() => console.log('✅ MongoDB connected'))
  .catch(err => console.error('❌ MongoDB connection error:', err));

// Routes
app.use('/api/inventory', require('./routes/inventory'));
app.use('/api/users', require('./routes/users'));

const PORT = 8080
app.listen(PORT, () => {
  console.log(`🌐 HTTP server running on http://localhost:${PORT}`);
});
