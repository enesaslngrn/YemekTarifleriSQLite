package com.enesas.yemektariflerisqlite

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.Navigation
import com.enesas.yemektariflerisqlite.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        var view = binding.root
        setContentView(view)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { // oluşturduğumuz menüyü buraya bağlayacağız.

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.yemek_ekle,menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // menüden bir şey seçilirse ne yapayım?

        if (item.itemId == R.id.yemek_ekleme_item){
            val action = ListeFragmentDirections.actionListeFragmentToTarifFragment("menudengeldim",0)
            Navigation.findNavController(this,R.id.fragmentContainerView).navigate(action)
        }

        return super.onOptionsItemSelected(item)
    }
}