package com.enesas.yemektariflerisqlite

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.enesas.yemektariflerisqlite.databinding.FragmentTarifBinding
import java.io.ByteArrayOutputStream

class TarifFragment : Fragment() {

    var secilenGorsel: Uri? = null
    var secilenBitmap: Bitmap? = null

    lateinit var binding: FragmentTarifBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTarifBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { // Eğer fragmentlar içerisinde bir buton ile işlem yapmak istersen. SetOnClickListener ile yapmak zorundasın
        super.onViewCreated(view, savedInstanceState)
        binding.button.setOnClickListener {
            kaydet(it)
        }
        binding.imageView.setOnClickListener {
            gorselSec(it)
        }

        arguments?.let {
            var gelenBilgi = TarifFragmentArgs.fromBundle(it).bilgi

            if (gelenBilgi.equals("menudengeldim")){
                // yeni bir yemek eklemeye geldi.
                binding.yemekIsmiText.setText("")
                binding.yemekMalzemeText.setText("")
                binding.button.visibility = View.VISIBLE // butonu görünür hale getirdi.

                val gorselSecmeArkaPlani = BitmapFactory.decodeResource(context?.resources,R.drawable.gorselsecimi)
                binding.imageView.setImageBitmap(gorselSecmeArkaPlani)

            }else{
                // daha önce oluşturulan yemeği görmeye geldi.
                binding.button.visibility = View.INVISIBLE // butonu görünmez hale getirdi.

                val secilenId = TarifFragmentArgs.fromBundle(it).id


                context?.let {

                    try {

                        val db = it.openOrCreateDatabase("Yemekler",Context.MODE_PRIVATE,null)
                        val cursor = db.rawQuery("SELECT * FROM yemekler WHERE id = ?", arrayOf(secilenId.toString()))

                        val yemekIsmiIndex = cursor.getColumnIndex("yemekismi")
                        val yemekMalzemeIndex = cursor.getColumnIndex("yemekmalzemesi")
                        val yemekGorseli = cursor.getColumnIndex("gorsel")

                        while (cursor.moveToNext()){
                            binding.yemekIsmiText.setText(cursor.getString(yemekIsmiIndex))
                            binding.yemekMalzemeText.setText(cursor.getString(yemekMalzemeIndex))

                            val byteDizisi = cursor.getBlob(yemekGorseli)
                            val bitmap = BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)
                            binding.imageView.setImageBitmap(bitmap)
                        }

                        cursor.close()

                    }catch (e:Exception){
                        e.printStackTrace()
                    }

                }

            }
        }
    }

    fun kaydet(view:View){ // yani sadece bu şekilde yapamıyoruz. Önce bu fonksiyonu oluşturup sonra SetOnClickListener içinde çağıracağız
        //Sqlite kaydetme kısmı


        val yemekIsmi = binding.yemekIsmiText.text.toString()
        val yemekMalzemeleri = binding.yemekMalzemeText.text.toString()

        if (secilenBitmap != null){
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!!,500) // 300 iyi bir sayı. Bu sayede 1mb'ın altında kalıyor. Ama deneyerek değiştirebilirsin.
            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteDizisi = outputStream.toByteArray()

            try {
                context?.let {
                    val database = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS yemekler(id INTEGER PRIMARY KEY, yemekismi VARCHAR, yemekmalzemesi VARCHAR, gorsel BLOB)")

                    val sqlString = "INSERT INTO yemekler (yemekismi,yemekmalzemesi,gorsel) VALUES(?, ?, ?)"

                    val statement = database.compileStatement(sqlString) // bunun sayesinde yukarıdaki soru işaretleri yerine veri atayabiliriz.
                    statement.bindString(1,yemekIsmi) // !!!!!BURADA INDEX 0 DAN DEĞİL 1 DEN BAŞLAR.
                    statement.bindString(2,yemekMalzemeleri)
                    statement.bindBlob(3,byteDizisi)

                    statement.execute()

                }
            }catch (e: Exception){
                e.printStackTrace()
            }
            val action = TarifFragmentDirections.actionTarifFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }
    fun gorselSec(view: View) {
        //Burda kullanıcıdan izin alacağız. Önce developer.android'e gidip manifest.permission yazıp ilgili izin için bir şeyler alman gerek.
        // Şimdi checkSelfPermission komutunu çağırmamız lazım. Ama bunu sadece aktiviteden çağırabiliriz. Biz ise fragment'tayız.
        // O yüzden ContextCompat diyip, hangi API ile çalışacağını umursamadan direkt devam edebiliyoruz. Yani API 19 öncesinde mi sonrasında mı 25 mi 30 mu önemli değil.

        activity?.let {
            if (ContextCompat.checkSelfPermission(it,Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED){
                // Galeriye gidilebilir.
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent,2)

            }else if(shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)){
                // Normalde bu fonksiyon olmadan zaten 2. kere izin otomatik istenecekti.
                // Burada kullanıcı eğer izni 1 kere reddeddiyse çıkacak olan ikinci izin isteğinde ona neden kabul etmesi gerektiğini açıklayan bir uyarı mesajı gönderiyoruz.
                // Eğer sonuç olarak 2 kere izin vermezse, artık görsele tıklayamıyoruz. Yani kullanıcı ayarlardan manuel olarak izni açması gerekli.

                val uyariMesaji = AlertDialog.Builder(it)
                uyariMesaji.setMessage("Uygulama'nın galeriye erişimi olmadan fotoğraf eklenemez! Devam etmek için izin veriniz.")
                uyariMesaji.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                    ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_MEDIA_IMAGES),1)
                    if (ContextCompat.checkSelfPermission(it,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(it,"'Sistem Ayarlarından' gerekli izni aktifleştirin!", Toast.LENGTH_LONG).show()
                    }
                })
                uyariMesaji.setNegativeButton("NO", DialogInterface.OnClickListener { dialog, which ->
                    Toast.makeText(it,"Erişim sağlanamadı!", Toast.LENGTH_LONG).show()
                })
                uyariMesaji.show()

            }else{
                // Henüz ilk kez izin isteniyor burada çünkü izin verilmemiş. Kullanıcıdan otomatik olarak her zaman 2 kere izin istenir. Bu İLK İSTENEN İZİN.
                ActivityCompat.requestPermissions(it,arrayOf(Manifest.permission.READ_MEDIA_IMAGES),1)
            }
        }
    }

    override fun onRequestPermissionsResult( //Burda izinlerin sonuçlarını kontrol edicez
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1){

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val galeriIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI) // bu kod ile galeriye gidiyoruz.
                startActivityForResult(galeriIntent,2)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){ //Galeriye gidip seçim yaparsa RESULT_OK. yapmaz çıkarsa RESULT_CANCELED
            secilenGorsel = data.data // Seçilen görsel ile telefonun neresinde durduğunu almış olduk. Yani Uri'yını aldık.
            //şimdi bu konumunu aldığımız görseli bitmap'e çevireceğiz.

            try {

                context?.let {
                    if (secilenGorsel != null){
                        if (Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(it.contentResolver,secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }else{
                            secilenBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver,secilenGorsel)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }

                    }
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }



    fun kucukBitmapOlustur(kullanicininSectigiBitmap: Bitmap, maximumBoyut: Int): Bitmap{
        //Sqlite' a 1mb dan fazla veri kaydedemeyiz. Bu yüzden kullanıcının kaç mb'lık fotoğraf çektiğini bilmediğimiz için, yüklenen fotonun boyutunu belirlemek gerek.

        var width = kullanicininSectigiBitmap.width
        var height = kullanicininSectigiBitmap.height

        val bitmapOrani : Double = width.toDouble() / height.toDouble() // eğer sonuç 1 den büyük çıkarsa width > height demek yani yatay bi fotoğraf olmuş olur.

        if (bitmapOrani > 1) {
            //görsel yatay
            width = maximumBoyut
            val kisaltilmisHeight =  width / bitmapOrani
            height = kisaltilmisHeight.toInt()
        }else{
            height = maximumBoyut
            val kisaltilmisWidth = height * bitmapOrani
            width = kisaltilmisWidth.toInt()
            //görsel dikey
        }
        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap,width,height,true)
    }


}