package com.bhamane.bridalstore

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bhamane.bridalstore.data.*
import com.bhamane.bridalstore.util.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity:ComponentActivity() { override fun onCreate(savedInstanceState:Bundle?) { super.onCreate(savedInstanceState); setContent { App() } } }
private fun dayStart(ms:Long):Long { val c=Calendar.getInstance();c.timeInMillis=ms;c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);return c.timeInMillis }
private fun dayEnd(ms:Long)=dayStart(ms)+86_399_999L
private fun date(ms:Long)=SimpleDateFormat("dd MMM yyyy",Locale.getDefault()).format(Date(ms))

@Composable fun App() {
 val context=LocalContext.current; val repo=remember{StoreRepository.get(context)}; val scope=rememberCoroutineScope()
 val sales by repo.sales.collectAsState(initial=emptyList()); val rentals by repo.rentals.collectAsState(initial=emptyList()); var page by remember{mutableStateOf(0)}
 MaterialTheme(colorScheme=lightColorScheme(primary=androidx.compose.ui.graphics.Color(0xFF8A5A00))) {
  Scaffold(bottomBar={
   NavigationBar {
    listOf("Home","Sale","Rental","Reports","History").forEachIndexed { i,n ->
     NavigationBarItem(
      selected=page==i,
      onClick={page=i},
      icon={Text(if(i==0)"⌂" else if(i==1)"₹" else if(i==2)"✦" else if(i==3)"▥" else "☰")},
      label={Text(n)}
     )
    }
   }
  }) { pad -> Box(Modifier.padding(pad)) { when(page){0->Home(sales,rentals,{page=1},{page=2},{page=3});1->SaleForm{d,c,p,a,items,disc->scope.launch{repo.createSale(d,c,p,a,items,disc);page=4}};2->RentalForm{item,photo,c,p,a,s,r,price->scope.launch{repo.createRental(item,photo,c,p,a,s,r,price);page=4}};3->Reports(repo,sales,rentals);4->History(sales,rentals)} } }
 }
}
@Composable fun Home(sales:List<SaleWithItems>,rentals:List<RentalEntity>,sale:()->Unit,rental:()->Unit,reports:()->Unit) { val today=dayStart(System.currentTimeMillis());val total=sales.filter{it.sale.date in today..dayEnd(today)}.sumOf{it.sale.finalTotal};LazyColumn(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){item{Text("Bhamane Bridal Store",style=MaterialTheme.typography.headlineMedium)};item{Text("Jewellery Sales & Rental Management")};item{Card(Modifier.fillMaxWidth()){Column(Modifier.padding(18.dp)){Text("Today's Sales");Text("₹$total",style=MaterialTheme.typography.headlineSmall);Text("Active rentals: ${rentals.count{it.status=="ACTIVE"}}")}}};item{Button(sale,Modifier.fillMaxWidth()){Text("New Sale")}};item{OutlinedButton(rental,Modifier.fillMaxWidth()){Text("New Rental")}};item{OutlinedButton(reports,Modifier.fillMaxWidth()){Text("Reports & Export")}}} }
@Composable fun SaleForm(save:(Long,String,String,String,List<Triple<String,Int,Double>>,Double)->Unit){var d by remember{mutableStateOf(System.currentTimeMillis())};var c by remember{mutableStateOf("")};var p by remember{mutableStateOf("")};var a by remember{mutableStateOf("")};var name by remember{mutableStateOf("")};var qty by remember{mutableStateOf("1")};var price by remember{mutableStateOf("")};var disc by remember{mutableStateOf("0")};val items=remember{mutableStateListOf<Triple<String,Int,Double>>()};val context=LocalContext.current;LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{Text("New Sale",style=MaterialTheme.typography.headlineMedium)};item{Button({pickDate(context,d){d=it}}){Text("Sale Date: ${date(d)}")}};item{Field("Customer Name",c){c=it};Field("Phone",p){p=it};Field("Address",a){a=it}};item{Text("Items",style=MaterialTheme.typography.titleLarge)};item{Field("Item Name",name){name=it}};item{Row{Field("Qty",qty,Modifier.weight(1f)){qty=it};Spacer(Modifier.width(8.dp));Field("Unit Price",price,Modifier.weight(1f)){price=it}}};item{Button({if(name.isNotBlank()){items.add(Triple(name,qty.toIntOrNull()?:1,price.toDoubleOrNull()?:0.0));name="";price=""}}){Text("Add Item")}};items(items.size){i->Text("${items[i].first} × ${items[i].second} = ₹${items[i].second*items[i].third}")};item{Field("Discount",disc){disc=it}};val sub=items.sumOf{it.second*it.third};item{Text("Subtotal: ₹$sub");Text("Final: ₹${(sub-(disc.toDoubleOrNull()?:0.0)).coerceAtLeast(0.0)}",style=MaterialTheme.typography.titleLarge)};item{Button({save(d,c,p,a,items.toList(),disc.toDoubleOrNull()?:0.0)},Modifier.fillMaxWidth(),enabled=c.isNotBlank()&&items.isNotEmpty()){Text("Finalize & Save Bill")}}} }
@Composable fun RentalForm(save:(String,String?,String,String,String,Long,Long,Double)->Unit){var item by remember{mutableStateOf("")};var photo by remember{mutableStateOf<String?>(null)};var c by remember{mutableStateOf("")};var p by remember{mutableStateOf("")};var a by remember{mutableStateOf("")};var s by remember{mutableStateOf(System.currentTimeMillis())};var r by remember{mutableStateOf(System.currentTimeMillis()+86400000)};var price by remember{mutableStateOf("")};val context=LocalContext.current;val picker=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){u:Uri?->photo=u?.toString()};LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{Text("New Rental",style=MaterialTheme.typography.headlineMedium)};item{Button({picker.launch("image/*")}){Text(if(photo==null)"Select Jewellery Photo" else "Photo Selected ✓")}};item{Field("Item Name",item){item=it};Field("Customer Name",c){c=it};Field("Phone",p){p=it};Field("Address",a){a=it}};item{Button({pickDate(context,s){s=it}}){Text("Rental Start: ${date(s)}")};Button({pickDate(context,r){r=it}}){Text("Return Date: ${date(r)}")}};item{Field("Rental Price",price){price=it}};item{Button({save(item,photo,c,p,a,s,r,price.toDoubleOrNull()?:0.0)},Modifier.fillMaxWidth(),enabled=item.isNotBlank()&&c.isNotBlank()){Text("Save Rental")}}} }
@Composable fun Reports(repo:StoreRepository,sales:List<SaleWithItems>,rentals:List<RentalEntity>){val context=LocalContext.current;val scope=rememberCoroutineScope();var from by remember{mutableStateOf(dayStart(System.currentTimeMillis()))};var to by remember{mutableStateOf(dayEnd(System.currentTimeMillis()))};var totals by remember{mutableStateOf(Pair(0.0,0.0))};LaunchedEffect(from,to){totals=repo.totals(from,to)};LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Text("Reports",style=MaterialTheme.typography.headlineMedium)};item{Text("From: ${date(from)}");Button({pickDate(context,from){from=dayStart(it)}}){Text("Change From")};Text("To: ${date(to)}");Button({pickDate(context,to){to=dayEnd(it)}}){Text("Change To")}};item{Card(Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text("Sales: ₹${totals.first}");Text("Rental Income: ₹${totals.second}");Text("Total: ₹${totals.first+totals.second}")}}};item{Button({scope.launch{ExcelExporter.share(context,ExcelExporter.export(context,repo.allSales(),repo.allRentals()))}},Modifier.fillMaxWidth()){Text("Export All Bills to Excel")}};item{Button({val x=sales.filter{it.sale.date in from..to};val y=rentals.filter{it.startDate in from..to};WhatsAppReporter.send(context,"${date(from)} - ${date(to)}",x.sumOf{it.sale.finalTotal},y.sumOf{it.rentalPrice},x.size,y.size)},Modifier.fillMaxWidth()){Text("Send Report to WhatsApp")}}} }
@Composable fun History(sales:List<SaleWithItems>,rentals:List<RentalEntity>){LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{Text("Bills & Rentals",style=MaterialTheme.typography.headlineMedium)};item{Text("Sales",style=MaterialTheme.typography.titleLarge)};items(sales.size){i->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text(sales[i].sale.billNumber);Text("${date(sales[i].sale.date)} • ${sales[i].sale.customerName}");Text("₹${sales[i].sale.finalTotal}")}}};item{Text("Rentals",style=MaterialTheme.typography.titleLarge)};items(rentals.size){i->Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text(rentals[i].rentalNumber);Text("${rentals[i].itemName} • ${rentals[i].customerName}");Text("Return ${date(rentals[i].returnDate)} • ₹${rentals[i].rentalPrice}")}}}} }
@Composable fun Field(label:String,value:String,modifier:Modifier=Modifier.fillMaxWidth(),change:(String)->Unit){OutlinedTextField(value,change,label={Text(label)},modifier=modifier)}
fun pickDate(context:Context,initial:Long,onPick:(Long)->Unit){val c=Calendar.getInstance();c.timeInMillis=initial;DatePickerDialog(context,{_,y,m,d->val x=Calendar.getInstance();x.set(y,m,d,0,0,0);x.set(Calendar.MILLISECOND,0);onPick(x.timeInMillis)},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show()}
