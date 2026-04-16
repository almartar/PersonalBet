package com.example.personalbet

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.personalbet.databinding.ActivityMainBinding
import com.example.personalbet.ui.add.AddBetFragment
import com.example.personalbet.ui.annual.AnnualSummaryFragment
import com.example.personalbet.ui.bets.BetsListFragment
import com.example.personalbet.ui.config.ConfigFragment
import com.example.personalbet.ui.stats.StatsFragment
import com.example.personalbet.ui.welcome.WelcomeFragment

/**
 * Navegación inferior como en el temario (UD07): FrameLayout + FragmentTransaction.replace
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, 0)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNav) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, bars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WelcomeFragment())
                .commit()
            binding.bottomNav.visibility = View.GONE
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_bets -> {
                    replaceMainFragment(BetsListFragment())
                    true
                }
                R.id.nav_stats -> {
                    replaceMainFragment(StatsFragment())
                    true
                }
                R.id.nav_annual -> {
                    replaceMainFragment(AnnualSummaryFragment())
                    true
                }
                R.id.nav_config -> {
                    replaceMainFragment(ConfigFragment())
                    true
                }
                else -> false
            }
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val showBottom = supportFragmentManager.backStackEntryCount == 0
            binding.bottomNav.visibility = if (showBottom) View.VISIBLE else View.GONE
        }
    }

    private fun replaceMainFragment(fragment: Fragment) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun openAddBetScreen() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AddBetFragment())
            .addToBackStack(null)
            .commit()
    }

    fun openEditBetScreen(betId: Long) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AddBetFragment.newEditInstance(betId))
            .addToBackStack(null)
            .commit()
    }

    fun openHomeFromWelcome() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, BetsListFragment())
            .commit()
        binding.bottomNav.visibility = View.VISIBLE
        binding.bottomNav.selectedItemId = R.id.nav_bets
    }
}
