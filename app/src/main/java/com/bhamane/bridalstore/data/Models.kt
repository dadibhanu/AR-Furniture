package com.bhamane.bridalstore.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Entity(tableName="sales") data class SaleEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val billNumber:String="",val date:Long,val customerName:String,val phone:String,val address:String,val subtotal:Double,val discount:Double,val finalTotal:Double,val createdAt:Long=System.currentTimeMillis())
@Entity(tableName="sale_items",foreignKeys=[ForeignKey(entity=SaleEntity::class,parentColumns=["id"],childColumns=["saleId"],onDelete=ForeignKey.CASCADE)],indices=[Index("saleId")]) data class SaleItemEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val saleId:Long,val name:String,val quantity:Int,val unitPrice:Double,val lineTotal:Double)
@Entity(tableName="rentals") data class RentalEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val rentalNumber:String="",val itemName:String,val photoUri:String?,val customerName:String,val phone:String,val address:String,val startDate:Long,val returnDate:Long,val rentalPrice:Double,val status:String="ACTIVE",val createdAt:Long=System.currentTimeMillis())
data class SaleWithItems(@Embedded val sale:SaleEntity,@Relation(parentColumn="id",entityColumn="saleId") val items:List<SaleItemEntity>)
@Dao interface StoreDao {
 @Transaction @Query("SELECT * FROM sales ORDER BY date DESC, id DESC") fun observeSales():Flow<List<SaleWithItems>>
 @Transaction @Query("SELECT * FROM sales ORDER BY date DESC, id DESC") suspend fun allSales():List<SaleWithItems>
 @Insert suspend fun insertSale(sale:SaleEntity):Long
 @Insert suspend fun insertItems(items:List<SaleItemEntity>)
 @Update suspend fun updateSale(sale:SaleEntity)
 @Query("SELECT * FROM rentals ORDER BY returnDate ASC") fun observeRentals():Flow<List<RentalEntity>>
 @Query("SELECT * FROM rentals ORDER BY returnDate ASC") suspend fun allRentals():List<RentalEntity>
 @Insert suspend fun insertRental(rental:RentalEntity):Long
 @Update suspend fun updateRental(rental:RentalEntity)
 @Query("SELECT COALESCE(SUM(finalTotal),0) FROM sales WHERE date BETWEEN :from AND :to") suspend fun salesTotal(from:Long,to:Long):Double
 @Query("SELECT COALESCE(SUM(rentalPrice),0) FROM rentals WHERE startDate BETWEEN :from AND :to") suspend fun rentalTotal(from:Long,to:Long):Double
 @Query("SELECT * FROM rentals WHERE status='ACTIVE' AND returnDate <= :before") suspend fun dueRentals(before:Long):List<RentalEntity>
}
@Database(entities=[SaleEntity::class,SaleItemEntity::class,RentalEntity::class],version=1,exportSchema=false) abstract class StoreDatabase:RoomDatabase(){abstract fun dao():StoreDao}
