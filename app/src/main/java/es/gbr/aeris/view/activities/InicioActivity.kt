package es.gbr.aeris.view.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.gbr.aeris.R
import es.gbr.aeris.ui.theme.AerisTheme


class InicioActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val esTemaOscuro = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        setContent {
            AerisTheme(darkTheme = esTemaOscuro) {
                PantallaInicio(
                    alPulsarEntrar = {
                        val intent = Intent(this, MainActivity::class.java)
                        val bundle = Bundle()
                        bundle.putBoolean("usarFahrenheit", false)
                        bundle.putBoolean("usarMph", false)
                        bundle.putBoolean("temaOscuro", esTemaOscuro)
                        intent.putExtras(bundle)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun PantallaInicio(
    alPulsarEntrar: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(0.25f))
                
                Image(
                    painter = painterResource(R.drawable.ic_nube_inicio),
                    contentDescription = stringResource(R.string.app_name),
                    modifier = Modifier.size(width = 346.dp, height = 241.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = stringResource(R.string.inicio_titulo),
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.weight(0.5f))
                
                Button(
                    onClick = alPulsarEntrar,
                    modifier = Modifier
                        .width(301.dp)
                        .height(45.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.inicio_entrar),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
