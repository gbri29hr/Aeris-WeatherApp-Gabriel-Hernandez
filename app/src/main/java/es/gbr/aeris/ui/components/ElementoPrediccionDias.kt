package es.gbr.aeris.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.gbr.aeris.model.database.entities.PrediccionDiariaEntidad

@Composable
fun ElementoPrediccionDias(
    prediccion: PrediccionDiariaEntidad,
    diaTraducido: String,
    diaSiguiente: String,
    usarFahrenheit: Boolean,
    iconoRes: Int,
    modificador: Modifier = Modifier
) {
    val tempAlta = if (usarFahrenheit) (prediccion.tempAlta * 9/5) + 32 else prediccion.tempAlta
    val tempBaja = if (usarFahrenheit) (prediccion.tempBaja * 9/5) + 32 else prediccion.tempBaja
    
    Row(
        modifier = modificador
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = diaTraducido,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = diaSiguiente,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        
        Icon(
            painter = painterResource(id = iconoRes),
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        
        Text(
            text = "${tempBaja.toInt()}° / ${tempAlta.toInt()}°",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(100.dp)
        )
    }
}
