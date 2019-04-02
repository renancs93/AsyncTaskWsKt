package br.edu.ifsp.scl.asynctaskwskt

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import br.edu.ifsp.scl.asynctaskwskt.MainActivity.constantes.URL_BASE
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Thread.sleep
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

    object constantes {
        val URL_BASE = "http://www.nobile.pro.br/sdm/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Seta o Listener para o botão usando uma função lambda
        buscarInformacoesBt.setOnClickListener {
            // Disparando AsyncTask para buscar texto
            val buscarTextoAt = BuscarTextoAt()

            buscarTextoAt.execute(URL_BASE + "texto.php")
//            buscarTextoAt.execute(URL_BASE + "data.php")
            buscarData(URL_BASE + "data.php")
        }

    }
    // AsyncTask que fará acesso ao WebService
    private inner class BuscarTextoAt : AsyncTask<String, Int, String>() {
        // Executa na mesma Thread de UI
        override fun onPreExecute() {
            super.onPreExecute()
            toast("Buscando String no Web Service")
            // Mostrando a barra de progresso
            progressBar?.visibility = View.VISIBLE
        }
        // Executa numa outra Thread em background
        override fun doInBackground(vararg params: String?): String {
            // Pegando URL na primeira posição do params
            val url = params[0]
            // Criando um StringBuffer para receber a resposta do Web Service
            val stringBufferResposta: StringBuffer = StringBuffer()
            try {
                // Criando uma conexão HTTP a partir da URL
                val conexao = URL(url).openConnection() as HttpURLConnection
                if (conexao.responseCode == HttpURLConnection.HTTP_OK) {
                    // Caso a conexão seja bem sucedida, resgata o InputStream da mesma
                    val inputStream = conexao.inputStream
                    // Cria um BufferedReader a partir do InputStream
                    val bufferedReader = BufferedInputStream(inputStream).bufferedReader()
                    // Lê o bufferedReader para uma lista de Strings
                    val respostaList = bufferedReader.readLines()
                    // "Appenda" cada String da lista ao StringBuffer
                    respostaList.forEach { stringBufferResposta.append(it) }
                }
            } catch (ioe: IOException) {
                toast("Erro na conexão!")
            }
            // Simulando notificação do progresso para Thread de UI
            for (i in 1..10) {
                /* Envia um inteiro para ser publicado como progresso. Esse valor é recebido pela função
                callback onProgressUpdate*/
                publishProgress(i)
                // Dormindo por 0.5 s para simular o atraso de rede
                sleep(500)
            }
            // Retorna a String formada a partir do StringBuffer
            return stringBufferResposta.toString()
        }
        // Executa na mesma Thread de UI
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            toast("Texto recuperado com sucesso")
            // Altera o TextView com o texto recuperado
            textoTv.text = result
            // Tornando a barra de progresso invisível
            progressBar?.visibility = View.GONE
        }
        // Executa na Thread de UI, é chamado sempre após uma publishProgress
        override fun onProgressUpdate(vararg values: Int?) {
            // Se o valor de progresso não for nulo, atualiza a barra de progresso
            values[0]?.apply { progressBar?.progress = this }
        }
    }

    private fun toast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun buscarData(url: String) {
        val buscaDataAS = object : AsyncTask<String, Void, JSONObject>() {
            override fun onPreExecute() {
                super.onPreExecute()
                progressBar.visibility = View.VISIBLE
            }

            override fun doInBackground(vararg strings: String): JSONObject? {
                var jsonObject: JSONObject? = null
                val sb = StringBuilder()
                try {
                    val url = strings[0]
                    val conexao = URL(url).openConnection() as HttpURLConnection
                    if (conexao.responseCode == HttpURLConnection.HTTP_OK) {
                        val iss = conexao.inputStream
                        val br = BufferedReader(InputStreamReader(iss))
                        //var temp: String
                        for (temp in br.readLine()) {
                            sb.append(temp)
                        }
                    }
                    jsonObject = JSONObject(sb.toString())
                } catch (ioe: IOException) {
                    ioe.printStackTrace()
                } catch (jsone: JSONException) {
                    jsone.printStackTrace()
                }

                return jsonObject
            }

            override fun onPostExecute(s: JSONObject) {
                var data: String? = null
                var hora: String? = null
                var ds: String? = null
                super.onPostExecute(s)
                try {
                    data = s.getInt("mday").toString() + "/" + s.getInt("mon") + "/" + s.getInt("year")
                    hora = s.getInt("hours").toString() + ":" + s.getInt("minutes") + ":" + s.getInt("seconds")
                    ds = s.getString("weekday")
                } catch (jsone: JSONException) {
                    jsone.printStackTrace()
                }

                (tv_data as TextView).text = data + "\n" + hora + "\n" + ds
                progressBar.visibility = View.GONE
            }
        }
        buscaDataAS.execute(url)
    }

}