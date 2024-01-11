package com.enesas.yemektariflerisqlite

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.enesas.yemektariflerisqlite.databinding.FragmentListeBinding

class ListeFragment : Fragment() {
    var yemekIsmiListesi = ArrayList<String>()
    var yemekIdListesi = ArrayList<Int>()
    private lateinit var listeAdapter: ListeRecyclerAdapter

    lateinit var binding: FragmentListeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentListeBinding.inflate(inflater,container,false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listeAdapter = ListeRecyclerAdapter(yemekIsmiListesi,yemekIdListesi)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = listeAdapter

        sqlVeriAlma()

    }

    fun sqlVeriAlma(){
        try {
            activity?.let{
                val database = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)
                val cursor = database.rawQuery("SELECT * FROM yemekler", null)

                val yemekIsmiIndex = cursor.getColumnIndex("yemekismi")
                val yemekIdIndex = cursor.getColumnIndex("id")

                yemekIsmiListesi.clear()
                yemekIdListesi.clear()

                while (cursor.moveToNext()){
                    yemekIsmiListesi.add(cursor.getString(yemekIsmiIndex))
                    yemekIdListesi.add(cursor.getInt(yemekIdIndex))
                }
                listeAdapter.notifyDataSetChanged() // yeni veri geldikçe değişmesini sağlayacak listenin.
                cursor.close()
            }


        }catch (e:Exception){
            e.printStackTrace()
        }


    }
}