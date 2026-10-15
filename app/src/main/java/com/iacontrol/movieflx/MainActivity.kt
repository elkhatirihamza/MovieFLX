package com.iacontrol.movieflx

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.iacontrol.movieflx.R
import com.iacontrol.movieflx.databinding.ActivityMainBinding

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Récupérer le NavController
        // Le NavController est associé au NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 2. Lier la BottomNavigationView au NavController
        // L'ID des éléments du menu (moviesFragment, likedMoviesFragment) DOIT
        // correspondre aux IDs des destinations dans nav_graph.xml.
        binding.bottomNav.setupWithNavController(navController)
        //

        // 3. Gérer la Visibilité de la Bottom Nav
        // La barre doit être masquée sur les écrans d'authentification (Welcome, Login, Signup)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.moviesFragment, R.id.likedMoviesFragment -> {
                    // Montrer la Bottom Nav sur les écrans de niveau supérieur
                    binding.bottomNav.visibility = View.VISIBLE
                }
                else -> {

                    binding.bottomNav.visibility = View.GONE
                }
            }
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        // Demande au NavController de naviguer vers la destination parente.
        // Si la navigation réussit, on retourne true. Sinon, on laisse le comportement par défaut.
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}