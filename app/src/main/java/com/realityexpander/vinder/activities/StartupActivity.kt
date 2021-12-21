package com.realityexpander.vinder.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.get
import com.lorentzos.flingswipe.SwipeFlingAdapterView
import com.realityexpander.vinder.R

const val TAG = "StartupActivity"

class StartupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)
    }

    fun onLogin(v: View) {
        startActivity(LoginActivity.newIntent(this))
    }

    fun onSignup(v: View) {
//        startActivity(SignupActivity.newIntent(this))
    }

    companion object {
        fun newIntent(context: Context?) = Intent(context, StartupActivity::class.java)
    }


}


//val flingContainer: SwipeFlingAdapterView = findViewById<SwipeFlingAdapterView>(R.id.frame);
//val al = ArrayList<String>();
//
//        al.add("php");
//        al.add("c");
//        al.add("python");
//        al.add("java");
//
//        //choose your favorite adapter
//        val arrayAdapter = ArrayAdapter<String>(this, R.layout.item, R.id.helloText, al );
//
//        //set the listener and the adapter
//        flingContainer.adapter = arrayAdapter;
//        flingContainer.setFlingListener(object: SwipeFlingAdapterView.onFlingListener {
//            var i = 0
//
//            override fun removeFirstObjectInAdapter() {
//                // this is the simplest way to delete an object from the Adapter (/AdapterView)
//                Log.d("LIST", "removed object!");
//                al.removeAt(0);
//                arrayAdapter.notifyDataSetChanged();
//            }
//
//            override fun onLeftCardExit(dataObject: Any?) {
//                //Do something on the left!
//                //You also have access to the original object.
//                //If you want to use it just cast it (String) dataObject
//                Toast.makeText(this@StartupActivity, "Left!", Toast.LENGTH_SHORT).show();
//            }
//
//            override fun onRightCardExit(dataObject: Any?) {
//                Toast.makeText(this@StartupActivity, "Right!", Toast.LENGTH_SHORT).show();
//            }
//
//            override fun onAdapterAboutToEmpty(itemsInAdapter: Int) {
//                // Ask for more data here
//                al.add("XML $i")
//                arrayAdapter.notifyDataSetChanged()
//                Log.d("LIST", "notified")
//                i++
//            }
//
//            override fun onScroll(p0: Float) {
//                flingContainer[flingContainer.childCount-1].alpha=(1.0f-p0/5.0f)
//            }
//        })
//
//        // Optionally add an OnItemClickListener
//        flingContainer.setOnItemClickListener { position, dataObject ->
//            Toast.makeText(
//                this@StartupActivity,
//                "Clicked! $position $dataObject",
//                Toast.LENGTH_SHORT
//            ).show()
//        }